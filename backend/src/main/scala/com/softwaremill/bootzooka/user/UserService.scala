package com.softwaremill.bootzooka.user

import cats.MonadError
import cats.implicits._
import com.softwaremill.bootzooka._
import com.softwaremill.bootzooka.email.{EmailData, EmailScheduler, EmailTemplates}
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.logging.FLogging
import com.softwaremill.bootzooka.security.{ApiKey, ApiKeyService}
import com.softwaremill.bootzooka.util._
import com.softwaremill.tagging.@@
import tsec.common.Verified

import scala.concurrent.duration.Duration

class UserService(
    userModel: UserModel,
    emailScheduler: EmailScheduler,
    emailTemplates: EmailTemplates,
    apiKeyService: ApiKeyService,
    idGenerator: IdGenerator,
    clock: Clock,
    config: UserConfig
) extends FLogging {

  private val LoginAlreadyUsed = "Login already in use!"
  private val EmailAlreadyUsed = "E-mail already in use!"
  private val IncorrectLoginOrPassword = "Incorrect login/email or password"

  def registerNewUser(login: String, email: String, password: String): ConnectionIO[ApiKey] = {
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

    def doRegister(): ConnectionIO[ApiKey] = for {
      id <- idGenerator.nextId[ConnectionIO, User]()
      now <- clock.now[ConnectionIO]()
      user = User(id, loginClean, loginClean.lowerCased, emailClean.lowerCased, User.hashPassword(password), now)
      confirmationEmail = emailTemplates.registrationConfirmation(loginClean)
      _ <- logger.debug[ConnectionIO](s"Registering new user: ${user.emailLowerCased}, with id: ${user.id}")
      _ <- userModel.insert(user)
      _ <- emailScheduler(EmailData(emailClean, confirmationEmail))
      apiKey <- apiKeyService.create(user.id, config.defaultApiKeyValid)
    } yield apiKey

    for {
      _ <- UserValidator(Some(loginClean), Some(emailClean), Some(password)).as[ConnectionIO]
      _ <- checkUserDoesNotExist()
      apiKey <- doRegister()
    } yield apiKey
  }

  def findById(id: Id @@ User): ConnectionIO[User] = userOrNotFound(userModel.findById(id))

  def login(loginOrEmail: String, password: String, apiKeyValid: Option[Duration]): ConnectionIO[ApiKey] = {
    val loginOrEmailClean = loginOrEmail.trim()
    for {
      user <- userOrNotFound(userModel.findByLoginOrEmail(loginOrEmailClean.lowerCased))
      _ <- verifyPassword(user, password, validationErrorMsg = IncorrectLoginOrPassword)
      apiKey <- apiKeyService.create(user.id, apiKeyValid.getOrElse(config.defaultApiKeyValid))
    } yield apiKey
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
            _ <- logger.debug[ConnectionIO](s"Changing login for user: $userId, to: $newLoginClean")
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
            _ <- logger.debug[ConnectionIO](s"Changing email for user: $userId, to: $newEmailClean")
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

    def sendMail(user: User): ConnectionIO[Unit] = {
      val confirmationEmail = emailTemplates.profileDetailsChangeNotification(user.login)
      emailScheduler(EmailData(user.emailLowerCased, confirmationEmail))
    }

    doChange().flatMap { anyUpdate =>
      if (anyUpdate) {
        findById(userId).flatMap(user => sendMail(user))
      } else {
        ().pure[ConnectionIO]
      }
    }
  }

  def changePassword(userId: Id @@ User, currentPassword: String, newPassword: String): ConnectionIO[Unit] = {
    def validateNewPassword() =
      UserValidator(None, None, Some(newPassword)).as[ConnectionIO]

    for {
      user <- userOrNotFound(userModel.findById(userId))
      _ <- verifyPassword(user, currentPassword, validationErrorMsg = "Incorrect current password")
      _ <- validateNewPassword()
      _ <- logger.debug[ConnectionIO](s"Changing password for user: $userId")
      _ <- userModel.updatePassword(userId, User.hashPassword(newPassword))
      confirmationEmail = emailTemplates.passwordChangeNotification(user.login)
      _ <- emailScheduler(EmailData(user.emailLowerCased, confirmationEmail))
    } yield ()
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
