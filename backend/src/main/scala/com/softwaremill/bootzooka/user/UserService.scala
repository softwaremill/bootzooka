package com.softwaremill.bootzooka.user

import cats.MonadError
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, LiftIO}
import cats.free.Free
import cats.implicits._
import com.softwaremill.bootzooka._
import com.softwaremill.bootzooka.email.{EmailData, EmailScheduler, EmailTemplates}
import com.softwaremill.bootzooka.infrastructure.Doobie
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.security.{ApiKey, ApiKeyService}
import com.softwaremill.bootzooka.util._
import com.softwaremill.tagging.@@
import com.typesafe.scalalogging.StrictLogging
import doobie.free.connection
import tsec.common.Verified

import scala.concurrent.duration.Duration

class UserService(
    userModel: UserModel,
    emailScheduler: EmailScheduler,
    emailTemplates: EmailTemplates,
    apiKeyService: ApiKeyService,
    idGenerator: IdGenerator,
    clock: Clock,
    config: UserConfig,
    xa: Transactor[IO]
) extends StrictLogging {

  private val LoginAlreadyUsed = "Login already in use!"
  private val EmailAlreadyUsed = "E-mail already in use!"
  private val IncorrectLoginOrPassword = "Incorrect login/email or password"

  def registerNewUser(login: String, email: String, password: String): IO[ConnectionIO[ApiKey]] = {
    val loginClean = login.trim()
    val emailClean = email.trim()

    def failIfDefined(op: ConnectionIO[Option[User]], msg: String): ConnectionIO[Unit] = {
      op.flatMap {
        case None    => ().pure[ConnectionIO]
        case Some(_) => Fail.IncorrectInput(msg).raiseError[ConnectionIO, Unit]
      }
    }

    def checkUserDoesNotExist(): ConnectionIO[Unit] = {
      failIfDefined(userModel.findByLogin(loginClean.lowerCased), LoginAlreadyUsed) >>
        failIfDefined(userModel.findByEmail(emailClean.lowerCased), EmailAlreadyUsed)
    }

    def doRegister(): IO[ConnectionIO[ApiKey]] = {
      val apiKeyIO  = for {
        id <- idGenerator.nextId[User]()
        now <- clock.now()
      } yield {
        val user = User(id, loginClean, loginClean.lowerCased, emailClean.lowerCased, User.hashPassword(password), now)
        val confirmationEmail = emailTemplates.registrationConfirmation(loginClean)
        logger.debug(s"Registering new user: ${user.emailLowerCased}, with id: ${user.id}")
        userModel.insert(user)
          .map(_ => emailScheduler(EmailData(emailClean, confirmationEmail)))
          .map(d =>
            d.flatMap(h =>
              h.map(_ => apiKeyService.create(user.id, config.defaultApiKeyValid))
                .transact(xa)
                .flatten
            )
          )
          .transact(xa)
          .flatten
      }
      apiKeyIO.flatten
    }

    val value = for {
      _ <- UserValidator(Some(loginClean), Some(emailClean), Some(password)).as[ConnectionIO]
      _ <- checkUserDoesNotExist()
    } yield doRegister()
    value.transact(xa).flatten
  }

  def findById(id: Id @@ User): ConnectionIO[User] = userOrNotFound(userModel.findById(id))

  def login(loginOrEmail: String, password: String, apiKeyValid: Option[Duration]): IO[ConnectionIO[ApiKey]] = {
    val loginOrEmailClean = loginOrEmail.trim()
    val value = for {
      user <- userOrNotFound(userModel.findByLoginOrEmail(loginOrEmailClean.lowerCased))
      _ <- verifyPassword(user, password, validationErrorMsg = IncorrectLoginOrPassword)
    } yield apiKeyService.create(user.id, apiKeyValid.getOrElse(config.defaultApiKeyValid))
    value.transact(xa).flatten
  }

  def changeUser(userId: Id @@ User, newLogin: String, newEmail: String): ConnectionIO[Unit] = {
    val newLoginClean = newLogin.trim()
    val newEmailClean = newEmail.trim()
    val newEmailLowerCased = newEmailClean.lowerCased

    def changeLogin(): ConnectionIO[Boolean] = {
      val newLoginLowerCased = newLoginClean.lowerCased
      userModel.findByLogin(newLoginLowerCased).flatMap {
        case Some(user) if user.id != userId           => Fail.IncorrectInput(LoginAlreadyUsed).raiseError[ConnectionIO, Boolean]
        case Some(user) if user.login == newLoginClean => false.pure[ConnectionIO]
        case _ =>
          for {
            _ <- validateLogin()
            _ = logger.debug(s"Changing login for user: $userId, to: $newLoginClean")
            _ <- userModel.updateLogin(userId, newLoginClean, newLoginLowerCased)
          } yield true
      }
    }

    def validateLogin() =
      UserValidator(Some(newLoginClean), None, None).as[ConnectionIO]

    def changeEmail(): ConnectionIO[Boolean] = {
      userModel.findByEmail(newEmailLowerCased).flatMap {
        case Some(user) if user.id != userId => Fail.IncorrectInput(EmailAlreadyUsed).raiseError[ConnectionIO, Boolean]
        case Some(user) if user.emailLowerCased == newEmailLowerCased => false.pure[ConnectionIO]
        case _ =>
          for {
            _ <- validateEmail()
            _ = logger.debug(s"Changing email for user: $userId, to: $newEmailClean")
            _ <- userModel.updateEmail(userId, newEmailLowerCased)
          } yield true
      }
    }

    def validateEmail() =
      UserValidator(None, Some(newEmailLowerCased), None).as[ConnectionIO]

    def doChange(): ConnectionIO[Boolean] = {
      for {
        loginUpdated <- changeLogin()
        emailUpdated <- changeEmail()
      } yield loginUpdated || emailUpdated
    }

    def sendMail(user: User): IO[ConnectionIO[Unit]] = {
      val confirmationEmail = emailTemplates.profileDetailsChangeNotification(user.login)
      emailScheduler(EmailData(user.emailLowerCased, confirmationEmail))
    }

    doChange() map { anyUpdate =>
      if (anyUpdate) {
        findById(userId).map(user => sendMail(user)).transact(xa).flatten
      } else {
        IO(().pure[ConnectionIO])
      }
    }
  }

  def changePassword(userId: Id @@ User, currentPassword: String, newPassword: String): IO[ConnectionIO[Unit]] = {
    def validateNewPassword() =
      UserValidator(None, None, Some(newPassword)).as[ConnectionIO]

    val value = for {
      user <- userOrNotFound(userModel.findById(userId))
      _ <- verifyPassword(user, currentPassword, validationErrorMsg = "Incorrect current password")
      _ <- validateNewPassword()
      _ = logger.debug(s"Changing password for user: $userId")
      _ <- userModel.updatePassword(userId, User.hashPassword(newPassword))
    } yield {
      val confirmationEmail = emailTemplates.passwordChangeNotification(user.login)
      emailScheduler(EmailData(user.emailLowerCased, confirmationEmail))
    }
    value.transact(xa).flatten
  }

  private def userOrNotFound(op: ConnectionIO[Option[User]]): ConnectionIO[User] = {
    op.flatMap {
      case Some(user) => user.pure[ConnectionIO]
      case None       => Fail.Unauthorized(IncorrectLoginOrPassword).raiseError[ConnectionIO, User]
    }
  }

  private def verifyPassword(user: User, password: String, validationErrorMsg: String): ConnectionIO[Unit] = {
    if (user.verifyPassword(password) == Verified) {
      ().pure[ConnectionIO]
    } else {
      Fail.Unauthorized(validationErrorMsg).raiseError[ConnectionIO, Unit]
    }
  }
}

object UserValidator {
  val MinLoginLength = 3
}

case class UserValidator(loginOpt: Option[String], emailOpt: Option[String], passwordOpt: Option[String]) {
  private val ValidationOk = Right(())

  private val emailRegex =
    """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  val result: Either[String, Unit] = {
    for {
      _ <- validateLogin(loginOpt)
      _ <- validateEmail(emailOpt)
      _ <- validatePassword(passwordOpt)
    } yield ()
  }

  def as[F[_]](implicit me: MonadError[F, Throwable]): F[Unit] =
    result.fold(msg => Fail.IncorrectInput(msg).raiseError[F, Unit], _ => ().pure[F])

  private def validateLogin(loginOpt: Option[String]): Either[String, Unit] =
    loginOpt.map(_.trim) match {
      case Some(login) =>
        if (login.length >= UserValidator.MinLoginLength) ValidationOk else Left("Login is too short!")
      case None => ValidationOk
    }

  private def validateEmail(emailOpt: Option[String]): Either[String, Unit] =
    emailOpt.map(_.trim) match {
      case Some(email) =>
        if (emailRegex.findFirstMatchIn(email).isDefined) ValidationOk else Left("Invalid e-mail format!")
      case None => ValidationOk
    }

  private def validatePassword(passwordOpt: Option[String]): Either[String, Unit] =
    passwordOpt.map(_.trim) match {
      case Some(password) =>
        if (password.nonEmpty) ValidationOk else Left("Password cannot be empty!")
      case None => ValidationOk
    }
}
