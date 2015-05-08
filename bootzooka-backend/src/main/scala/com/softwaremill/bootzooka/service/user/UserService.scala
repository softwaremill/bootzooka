package com.softwaremill.bootzooka.service.user

import com.softwaremill.bootzooka.dao.user.UserDao
import com.softwaremill.bootzooka.domain.User
import com.softwaremill.bootzooka.service.data.UserJson
import com.softwaremill.bootzooka.service.email.EmailScheduler
import com.softwaremill.bootzooka.service.templates.EmailTemplatingEngine
import java.util.UUID
import com.softwaremill.bootzooka.common.Utils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class UserService(userDao: UserDao, registrationDataValidator: RegistrationDataValidator, emailScheduler: EmailScheduler,
                  emailTemplatingEngine: EmailTemplatingEngine)(implicit ec: ExecutionContext) {

  def load(userId: userDao.UserId) = {
    userDao.load(userId).map(toUserJson)
  }

  def loadAll = userDao.loadAll().map(users => users.map(UserJson(_)))

  def registerNewUser(login: String, email: String, password: String): Future[Unit] = {
    val salt = Utils.randomString(128)
    val token = UUID.randomUUID().toString
    userDao.add(User(login, email.toLowerCase, password, salt, token)).map (_ => {
      val confirmationEmail = emailTemplatingEngine.registrationConfirmation(login)
      emailScheduler.scheduleEmail(email, confirmationEmail)
    })
  }

  def authenticate(login: String, nonEncryptedPassword: String): Future[Option[UserJson]] = {
    userDao.findByLoginOrEmail(login).map(userOpt =>
      toUserJson(userOpt.filter(u => User.passwordsMatch(nonEncryptedPassword, u))))
  }

  def authenticateWithToken(token: String): Future[Option[UserJson]] = userDao.findByToken(token).map(toUserJson)

  def findByLogin(login: String): Future[Option[UserJson]] = userDao.findByLowerCasedLogin(login).map(toUserJson)

  def findByEmail(email: String): Future[Option[UserJson]] = userDao.findByEmail(email.toLowerCase).map(toUserJson)

  private def toUserJson(userOpt: Option[User]) = userOpt.map(UserJson(_))

  def isUserDataValid(loginOpt: Option[String], emailOpt: Option[String], passwordOpt: Option[String]): Boolean = {
    registrationDataValidator.isDataValid(loginOpt, emailOpt, passwordOpt)
  }

  def checkUserExistenceFor(userLogin: String, userEmail: String): Future[Either[String, Unit]] = {
    val existingLoginFuture = findByLogin(userLogin)
    val existingEmailFuture = findByEmail(userEmail)

    for {
      existingLoginOpt <- existingLoginFuture
      existingEmailOpt <- existingEmailFuture
    } yield {
      existingLoginOpt.map(_ => Left("Login already in use!")).orElse(
        existingEmailOpt.map(_ => Left("E-mail already in use!"))).getOrElse(Right((): Unit))
    }
  }

  def changeLogin(currentLogin: String, newLogin: String): Future[Either[String, Unit]] = {
    findByLogin(newLogin).map {
      case Some(u) => Left("Login is already taken")
      case None => Right(userDao.changeLogin(currentLogin, newLogin))
    }
  }

  def changeEmail(currentEmail: String, newEmail: String): Future[Either[String, Unit]] = {
    findByEmail(newEmail).map {
      case Some(u) => Left("E-mail used by another user")
      case None => Right(userDao.changeEmail(currentEmail, newEmail))
    }
  }

  def changePassword(userToken: String, currentPassword: String, newPassword: String): Future[Either[String, Unit]] = {
    userDao.findByToken(userToken).map {
      case Some(u) => if (User.passwordsMatch(currentPassword, u)) {
        Right(userDao.changePassword(u.id, User.encryptPassword(newPassword, u.salt)))
      } else {
        Left("Current password is invalid")
      }
      case None => Left("User not found hence cannot change password")
    }
  }

}
