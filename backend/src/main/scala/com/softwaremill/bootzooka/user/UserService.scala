package com.softwaremill.bootzooka.user

import com.softwaremill.bootzooka.*
import com.softwaremill.bootzooka.email.{EmailData, EmailScheduler, EmailTemplates}
import com.softwaremill.bootzooka.infrastructure.Magnum.*
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.security.{ApiKey, ApiKeyService}
import com.softwaremill.bootzooka.util.*
import com.softwaremill.bootzooka.util.Strings.{Id, toLowerCased}
import ox.either
import ox.either.ok

import scala.concurrent.duration.Duration

class UserService(
    userModel: UserModel,
    emailScheduler: EmailScheduler,
    emailTemplates: EmailTemplates,
    apiKeyService: ApiKeyService,
    idGenerator: IdGenerator,
    clock: Clock,
    config: UserConfig
) extends Logging:
  private val LoginAlreadyUsed = "Login already in use!"
  private val EmailAlreadyUsed = "E-mail already in use!"
  private val IncorrectLoginOrPassword = "Incorrect login/email or password"

  def registerNewUser(login: String, email: String, password: String)(using DbTx): Either[Fail, ApiKey] =
    val loginClean = login.trim()
    val emailClean = email.trim()

    def failIfDefined(op: Option[User], msg: String): Either[Fail, Unit] =
      if op.isDefined then Left(Fail.IncorrectInput(msg)) else Right(())

    def checkUserDoesNotExist(): Either[Fail, Unit] = for {
      _ <- failIfDefined(userModel.findByLogin(loginClean.toLowerCased), LoginAlreadyUsed)
      _ <- failIfDefined(userModel.findByEmail(emailClean.toLowerCased), EmailAlreadyUsed)
    } yield ()

    def doRegister(): ApiKey =
      val id = idGenerator.nextId[User]()
      val now = clock.now()
      val user = User(id, loginClean, loginClean.toLowerCased, emailClean.toLowerCased, User.hashPassword(password), now)
      val confirmationEmail = emailTemplates.registrationConfirmation(loginClean)
      logger.debug(s"Registering new user: ${user.emailLowerCase}, with id: ${user.id}")
      userModel.insert(user)
      emailScheduler.schedule(EmailData(emailClean, confirmationEmail))
      apiKeyService.create(user.id, config.defaultApiKeyValid)

    either {
      UserValidator(Some(loginClean), Some(emailClean), Some(password)).result.ok()
      checkUserDoesNotExist().ok()
      doRegister()
    }
  end registerNewUser

  def findById(id: Id[User])(using DbTx): Either[Fail, User] = userOrNotFound(userModel.findById(id))

  def login(loginOrEmail: String, password: String, apiKeyValid: Option[Duration])(using DbTx): Either[Fail, ApiKey] = either {
    val loginOrEmailClean = loginOrEmail.trim()
    val user = userOrNotFound(userModel.findByLoginOrEmail(loginOrEmailClean.toLowerCased)).ok()
    verifyPassword(user, password, validationErrorMsg = IncorrectLoginOrPassword).ok()
    apiKeyService.create(user.id, apiKeyValid.getOrElse(config.defaultApiKeyValid))
  }

  def logout(id: Id[ApiKey])(using DbTx): Unit = apiKeyService.invalidate(id)

  def changeUser(userId: Id[User], newLogin: String, newEmail: String)(using DbTx): Either[Fail, Unit] =
    val newLoginClean = newLogin.trim()
    val newEmailClean = newEmail.trim()
    val newEmailtoLowerCased = newEmailClean.toLowerCased

    def changeLogin(): Either[Fail, Boolean] = {
      val newLogintoLowerCased = newLoginClean.toLowerCased
      userModel.findByLogin(newLogintoLowerCased) match {
        case Some(user) if user.id != userId           => Left(Fail.IncorrectInput(LoginAlreadyUsed))
        case Some(user) if user.login == newLoginClean => Right(false)
        case _ =>
          either {
            validateLogin().ok()
            logger.debug(s"Changing login for user: $userId, to: $newLoginClean")
            userModel.updateLogin(userId, newLoginClean, newLogintoLowerCased)
            true
          }
      }
    }

    def validateLogin() = UserValidator(Some(newLoginClean), None, None).result

    def changeEmail(): Either[Fail, Boolean] = {
      userModel.findByEmail(newEmailtoLowerCased) match {
        case Some(user) if user.id != userId                           => Left(Fail.IncorrectInput(EmailAlreadyUsed))
        case Some(user) if user.emailLowerCase == newEmailtoLowerCased => Right(false)
        case _ =>
          either {
            validateEmail().ok()
            logger.debug(s"Changing email for user: $userId, to: $newEmailClean")
            userModel.updateEmail(userId, newEmailtoLowerCased)
            true
          }
      }
    }

    def validateEmail() = UserValidator(None, Some(newEmailtoLowerCased), None).result

    def sendMail(user: User): Unit =
      val confirmationEmail = emailTemplates.profileDetailsChangeNotification(user.login)
      emailScheduler.schedule(EmailData(user.emailLowerCase, confirmationEmail))

    either {
      val loginUpdated = changeLogin().ok()
      val emailUpdated = changeEmail().ok()
      val anyUpdate = loginUpdated || emailUpdated
      if anyUpdate then sendMail(findById(userId).ok())
    }
  end changeUser

  def changePassword(userId: Id[User], currentPassword: String, newPassword: String)(using DbTx): Either[Fail, ApiKey] =
    def validateUserPassword(userId: Id[User], currentPassword: String): Either[Fail, User] = {
      for {
        user <- userOrNotFound(userModel.findById(userId))
        _ <- verifyPassword(user, currentPassword, validationErrorMsg = "Incorrect current password")
      } yield user
    }

    def validateNewPassword(): Either[Fail, Unit] = UserValidator(None, None, Some(newPassword)).result

    def updateUserPassword(user: User, newPassword: String): Unit =
      logger.debug(s"Changing password for user: ${user.id}")
      userModel.updatePassword(user.id, User.hashPassword(newPassword))
      val confirmationEmail = emailTemplates.passwordChangeNotification(user.login)
      emailScheduler.schedule(EmailData(user.emailLowerCase, confirmationEmail))

    def invalidateKeysAndCreateNew(user: User): ApiKey =
      apiKeyService.invalidateAllForUser(user.id)
      apiKeyService.create(user.id, config.defaultApiKeyValid)

    either {
      val user = validateUserPassword(userId, currentPassword).ok()
      validateNewPassword().ok()
      updateUserPassword(user, newPassword)
      invalidateKeysAndCreateNew(user)
    }
  end changePassword

  private def userOrNotFound(u: Option[User]): Either[Fail, User] = u match {
    case Some(user) => Right(user)
    case None       => Left(Fail.Unauthorized(IncorrectLoginOrPassword))
  }

  private def verifyPassword(user: User, password: String, validationErrorMsg: String): Either[Fail, Unit] =
    if user.verifyPassword(password) == PasswordVerificationStatus.Verified then Right(()) else Left(Fail.Unauthorized(validationErrorMsg))
end UserService

object UserValidator:
  val MinLoginLength = 3

case class UserValidator(loginOpt: Option[String], emailOpt: Option[String], passwordOpt: Option[String]):
  private val ValidationOk = Right(())

  private val emailRegex =
    """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  val result: Either[Fail, Unit] = (for {
    _ <- validateLogin(loginOpt)
    _ <- validateEmail(emailOpt)
    _ <- validatePassword(passwordOpt)
  } yield ()).left.map(Fail.IncorrectInput(_))

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
