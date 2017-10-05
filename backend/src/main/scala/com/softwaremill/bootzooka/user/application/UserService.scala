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
    emailTemplatingEngine: EmailTemplatingEngine
)(implicit ec: ExecutionContext, hashing: PasswordHashing)
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
        val salt          = Salt.newSalt()
        val now           = Instant.now().atOffset(ZoneOffset.UTC)
        val userAddResult = userDao.add(User.withRandomUUID(login, email.toLowerCase, password, salt, now))
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
      .map(_.filter(u => hashing.verifyPassword(u.password, nonEncryptedPassword, u.salt)))
      .flatMap {
        case Some(u) => rehashIfRequired(u, nonEncryptedPassword)
        case None    => Future.successful(None)
      }
      .map(_.map(BasicUserData.fromUser))

  def rehashIfRequired(u: User, nonEncryptedPassword: String): Future[Option[User]] =
    if (hashing.requiresRehashing(u.password)) {
      logger.debug("Rehashing")
      val newSalt     = Salt.newSalt()
      val newPassword = hashing.hashPassword(nonEncryptedPassword, newSalt)
      userDao.changePassword(u.id, newPassword, newSalt).map(_ => Some(u.copy(password = newPassword, salt = newSalt)))
    } else {
      logger.debug("Not rehashing")
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
        if (hashing.verifyPassword(u.password, currentPassword, u.salt)) {
          val salt = Salt.newSalt()
          userDao.changePassword(u.id, hashing.hashPassword(newPassword, salt), salt).map(Right(_))
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
