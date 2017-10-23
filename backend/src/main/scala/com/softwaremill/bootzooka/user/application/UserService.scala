package com.softwaremill.bootzooka.user.application

import java.time.{Instant, ZoneOffset}
import java.util.UUID

import com.softwaremill.bootzooka.common.crypto.{PasswordHashing, Salt}
import com.softwaremill.bootzooka.email.application.{EmailService, EmailTemplatingEngine}
import com.softwaremill.bootzooka.user._
import com.softwaremill.bootzooka.user.domain.{BasicUserData, User}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

class UserService(
    userDao: UserDao,
    emailService: EmailService,
    emailTemplatingEngine: EmailTemplatingEngine,
    passwordHashing: PasswordHashing
)(implicit ec: ExecutionContext)
    extends StrictLogging {

  def findById(userId: UserId): Future[Option[BasicUserData]] =
    userDao.findBasicDataById(userId)

  def registerNewUser(login: String, email: String, password: String): Future[UserRegisterResult] = {
    def checkUserExistence(): Future[Either[String, Unit]] = {
      val existingLoginFuture = userDao.findByLowerCasedLogin(login)
      val existingEmailFuture = userDao.findByEmail(email)

      for {
        existingLoginOpt <- existingLoginFuture
        existingEmailOpt <- existingEmailFuture
      } yield {
        existingLoginOpt
          .map(_ => Left("Login already in use!"))
          .orElse(
            existingEmailOpt.map(_ => Left("E-mail already in use!"))
          )
          .getOrElse(Right((): Unit))
      }
    }

    def registerValidData() = checkUserExistence().flatMap {
      case Left(msg) => Future.successful(UserRegisterResult.UserExists(msg))
      case Right(_) =>
        val salt = Salt.newSalt()
        val now  = Instant.now().atOffset(ZoneOffset.UTC)
        val userAddResult =
          userDao.add(User.withRandomUUID(login, email.toLowerCase, password, salt, now, passwordHashing))
        userAddResult.foreach { _ =>
          val confirmationEmail = emailTemplatingEngine.registrationConfirmation(login)
          emailService.scheduleEmail(email, confirmationEmail)
        }
        userAddResult.map(_ => UserRegisterResult.Success)
    }

    UserRegisterValidator
      .validate(login, email, password)
      .fold(
        msg => Future.successful(UserRegisterResult.InvalidData(msg)),
        _ => registerValidData()
      )
  }

  def authenticate(login: String, nonEncryptedPassword: String): Future[Option[BasicUserData]] =
    userDao
      .findByLoginOrEmail(login)
      .map(_.filter(u => passwordHashing.verifyPassword(u.password, nonEncryptedPassword, u.salt)))
      .flatMap {
        case Some(u) => rehashIfRequired(u, nonEncryptedPassword)
        case None    => Future.successful(None)
      }
      .map(_.map(BasicUserData.fromUser))

  /**
    * Some hash algorithms (like Argon2) can use parameters to affect how they work.
    * Typically these parameters are stored along the hash and they can change to speed up or slow down hashing
    * depending on needs and security status.
    *
    * It sounds like a good idea to check whether hashing parameters were changed recently after user successfully logs in
    * and if so, rehash user password using those new parameters. That way all user passwords are stored with up to date
    * security settings.
    */
  private def rehashIfRequired(u: User, password: String): Future[Option[User]] =
    if (passwordHashing.requiresRehashing(u.password)) {
      val newSalt     = Salt.newSalt()
      val newPassword = passwordHashing.hashPassword(password, newSalt)
      userDao.changePassword(u.id, newPassword, newSalt).map(_ => Some(u.copy(password = newPassword, salt = newSalt)))
    } else {
      Future.successful(Some(u))
    }

  def changeLogin(userId: UUID, newLogin: String): Future[Either[String, Unit]] =
    userDao.findByLowerCasedLogin(newLogin).flatMap {
      case Some(_) => Future.successful(Left("Login is already taken"))
      case None    => userDao.changeLogin(userId, newLogin).map(Right(_))
    }

  def changeEmail(userId: UUID, newEmail: String): Future[Either[String, Unit]] =
    userDao.findByEmail(newEmail).flatMap {
      case Some(_) => Future.successful(Left("E-mail used by another user"))
      case None    => userDao.changeEmail(userId, newEmail).map(Right(_))
    }

  def changePassword(userId: UUID, currentPassword: String, newPassword: String): Future[Either[String, Unit]] =
    userDao.findById(userId).flatMap {
      case Some(u) =>
        if (passwordHashing.verifyPassword(u.password, currentPassword, u.salt)) {
          val salt = Salt.newSalt()
          userDao.changePassword(u.id, passwordHashing.hashPassword(newPassword, salt), salt).map(Right(_))
        } else Future.successful(Left("Current password is invalid"))

      case None => Future.successful(Left("User not found hence cannot change password"))
    }
}

sealed trait UserRegisterResult

object UserRegisterResult {

  case class InvalidData(msg: String) extends UserRegisterResult

  case class UserExists(msg: String) extends UserRegisterResult

  case object Success extends UserRegisterResult

}

object UserRegisterValidator {
  private val ValidationOk = Right(())
  val MinLoginLength       = 3

  def validate(login: String, email: String, password: String): Either[String, Unit] =
    for {
      _ <- validLogin(login.trim).right
      _ <- validEmail(email.trim).right
      _ <- validPassword(password.trim).right
    } yield ()

  private def validLogin(login: String) =
    if (login.length >= MinLoginLength) ValidationOk else Left("Login is too short!")

  private val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  private def validEmail(email: String) =
    if (emailRegex.findFirstMatchIn(email).isDefined) ValidationOk else Left("Invalid e-mail!")

  private def validPassword(password: String) =
    if (password.nonEmpty) ValidationOk else Left("Password cannot be empty!")
}
