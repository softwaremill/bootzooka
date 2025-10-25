package com.softwaremill.bootzooka.user

import com.augustnagro.magnum.DbTx
import com.softwaremill.bootzooka.*
import com.softwaremill.bootzooka.email.{EmailData, EmailScheduler, EmailTemplates}
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.security.{ApiKey, ApiKeyService}
import com.softwaremill.bootzooka.util.*
import com.softwaremill.bootzooka.util.Strings.{Id, toLowerCased}
import ox.*
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
  // the messages that are returned to the user in case of validation failures
  private val LoginAlreadyUsed = "Login already in use!"
  private val EmailAlreadyUsed = "E-mail already in use!"
  private val IncorrectLoginOrPassword = "Incorrect login/email or password"

  def registerNewUser(login: String, email: String, password: String)(using DbTx): Either[Fail, ApiKey] =
    val loginClean = login.trim()
    val emailClean = email.trim()

    def failIfDefined(op: Option[User], msg: String): Either[Fail, Unit] =
      if op.isDefined then Left(Fail.IncorrectInput(msg)) else Right(())

    def checkUserDoesNotExist(): Either[Fail, Unit] = for
      _ <- failIfDefined(userModel.findByLogin(loginClean.toLowerCased), LoginAlreadyUsed)
      _ <- failIfDefined(userModel.findByEmail(emailClean.toLowerCased), EmailAlreadyUsed)
    yield ()

    def doRegister(): ApiKey =
      val id = idGenerator.nextId[User]()
      val now = clock.now()
      val user = User(id, loginClean, loginClean.toLowerCased, emailClean.toLowerCased, User.hashPassword(password), now)
      val confirmationEmail = emailTemplates.registrationConfirmation(loginClean)
      logger.debug(s"Registering new user: ${user.emailLowerCase}, with id: ${user.id}")
      userModel.insert(user)
      emailScheduler.schedule(EmailData(emailClean, confirmationEmail))
      apiKeyService.create(user.id, config.defaultApiKeyValid)
    end doRegister

    either:
      validateUserData(Some(loginClean), Some(emailClean), Some(password)).ok()
      // performing explicit checks in the DB to get nice, user-friendly error messages
      checkUserDoesNotExist().ok()
      doRegister()
  end registerNewUser

  def findById(id: Id[User])(using DbTx): Either[Fail, User] = userOrNotFound(userModel.findById(id))

  def login(loginOrEmail: String, password: String, apiKeyValid: Option[Duration])(using DbTx): Either[Fail, ApiKey] = either:
    val loginOrEmailClean = loginOrEmail.trim()
    val user = userOrNotFound(userModel.findByLoginOrEmail(loginOrEmailClean.toLowerCased)).ok()
    verifyPassword(user, password, validationErrorMsg = IncorrectLoginOrPassword).ok()
    apiKeyService.create(user.id, apiKeyValid.getOrElse(config.defaultApiKeyValid))

  def logout(id: Id[ApiKey])(using DbTx): Unit = apiKeyService.invalidate(id)

  def changeUser(userId: Id[User], newLogin: String, newEmail: String)(using DbTx): Either[Fail, Unit] =
    val newLoginClean = newLogin.trim()
    val newEmailClean = newEmail.trim()
    val newEmailtoLowerCased = newEmailClean.toLowerCased

    def changeLogin(): Either[Fail, Boolean] =
      val newLogintoLowerCased = newLoginClean.toLowerCased
      userModel.findByLogin(newLogintoLowerCased) match
        case Some(user) if user.id != userId           => Left(Fail.IncorrectInput(LoginAlreadyUsed))
        case Some(user) if user.login == newLoginClean => Right(false)
        case _                                         =>
          either:
            validateLogin().ok()
            logger.debug(s"Changing login for user: $userId, to: $newLoginClean")
            userModel.updateLogin(userId, newLoginClean, newLogintoLowerCased)
            true
      end match
    end changeLogin

    def validateLogin() = validateUserData(Some(newLoginClean), None, None)

    def changeEmail(): Either[Fail, Boolean] =
      userModel.findByEmail(newEmailtoLowerCased) match
        case Some(user) if user.id != userId                           => Left(Fail.IncorrectInput(EmailAlreadyUsed))
        case Some(user) if user.emailLowerCase == newEmailtoLowerCased => Right(false)
        case _                                                         =>
          either:
            validateEmail().ok()
            logger.debug(s"Changing email for user: $userId, to: $newEmailClean")
            userModel.updateEmail(userId, newEmailtoLowerCased)
            true

    def validateEmail() = validateUserData(None, Some(newEmailtoLowerCased), None)

    def sendMail(user: User): Unit =
      val confirmationEmail = emailTemplates.profileDetailsChangeNotification(user.login)
      emailScheduler.schedule(EmailData(user.emailLowerCase, confirmationEmail))

    either:
      val loginUpdated = changeLogin().ok()
      val emailUpdated = changeEmail().ok()
      val anyUpdate = loginUpdated || emailUpdated
      if anyUpdate then sendMail(findById(userId).ok())
  end changeUser

  def changePassword(userId: Id[User], currentPassword: String, newPassword: String)(using DbTx): Either[Fail, ApiKey] =
    def validateUserPassword(userId: Id[User], currentPassword: String): Either[Fail, User] =
      for
        user <- userOrNotFound(userModel.findById(userId))
        _ <- verifyPassword(user, currentPassword, validationErrorMsg = "Incorrect current password")
      yield user

    def validateNewPassword(): Either[Fail, Unit] = validateUserData(None, None, Some(newPassword))

    def updateUserPassword(user: User, newPassword: String): Unit =
      logger.debug(s"Changing password for user: ${user.id}")
      userModel.updatePassword(user.id, User.hashPassword(newPassword))
      val confirmationEmail = emailTemplates.passwordChangeNotification(user.login)
      emailScheduler.schedule(EmailData(user.emailLowerCase, confirmationEmail))

    def invalidateKeysAndCreateNew(user: User): ApiKey =
      apiKeyService.invalidateAllForUser(user.id)
      apiKeyService.create(user.id, config.defaultApiKeyValid)

    either:
      val user = validateUserPassword(userId, currentPassword).ok()
      validateNewPassword().ok()
      updateUserPassword(user, newPassword)
      invalidateKeysAndCreateNew(user)
  end changePassword

  private def userOrNotFound(u: Option[User]): Either[Fail, User] = u match
    case Some(user) => Right(user)
    case None       => Left(Fail.Unauthorized(IncorrectLoginOrPassword))

  private def verifyPassword(user: User, password: String, validationErrorMsg: String): Either[Fail, Unit] =
    if user.verifyPassword(password) == PasswordVerificationStatus.Verified then Right(()) else Left(Fail.Unauthorized(validationErrorMsg))
end UserService

private val ValidationOk = Right(())
private[user] val MinLoginLength = 3
private val emailRegex =
  """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

private[user] def validateUserData(loginOpt: Option[String], emailOpt: Option[String], passwordOpt: Option[String]): Either[Fail, Unit] =
  def validateLogin(loginOpt: Option[String]): Either[String, Unit] =
    loginOpt.map(_.trim) match
      case Some(login) => if login.length >= MinLoginLength then ValidationOk else Left("Login is too short!")
      case None        => ValidationOk

  def validateEmail(emailOpt: Option[String]): Either[String, Unit] =
    emailOpt.map(_.trim) match
      case Some(email) => if emailRegex.findFirstMatchIn(email).isDefined then ValidationOk else Left("Invalid e-mail format!")
      case None        => ValidationOk

  def validatePassword(passwordOpt: Option[String]): Either[String, Unit] =
    passwordOpt.map(_.trim) match
      case Some(password) => if password.nonEmpty then ValidationOk else Left("Password cannot be empty!")
      case None           => ValidationOk

  (for
    _ <- validateLogin(loginOpt)
    _ <- validateEmail(emailOpt)
    _ <- validatePassword(passwordOpt)
  yield ()).left.map(Fail.IncorrectInput(_))
end validateUserData
