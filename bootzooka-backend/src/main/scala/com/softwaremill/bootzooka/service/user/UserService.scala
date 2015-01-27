package com.softwaremill.bootzooka.service.user

import com.softwaremill.bootzooka.dao.user.UserDao
import com.softwaremill.bootzooka.domain.User
import com.softwaremill.bootzooka.service.data.UserJson
import com.softwaremill.bootzooka.service.email.EmailScheduler
import com.softwaremill.bootzooka.service.templates.EmailTemplatingEngine
import java.util.UUID
import com.softwaremill.bootzooka.common.Utils

class UserService(userDao: UserDao, registrationDataValidator: RegistrationDataValidator, emailScheduler: EmailScheduler,
                  emailTemplatingEngine: EmailTemplatingEngine) {

  def load(userId: userDao.UserId) = {
    UserJson(userDao.load(userId))
  }

  def loadAll = {
    UserJson(userDao.loadAll)
  }

  def count(): Long = {
    userDao.countItems()
  }

  def registerNewUser(login: String, email: String, password: String) {
    val salt = Utils.randomString(128)
    val token = UUID.randomUUID().toString
    userDao.add(User(login, email.toLowerCase, password, salt, token))
    val confirmationEmail = emailTemplatingEngine.registrationConfirmation(login)
    emailScheduler.scheduleEmail(email, confirmationEmail)
  }

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    val userOpt: Option[User] = userDao.findByLoginOrEmail(login)
    userOpt match {
      case Some(u) => {
        if (User.passwordsMatch(nonEncryptedPassword, u)) {
          UserJson(userOpt)
        } else {
          None
        }
      }
      case _ => None
    }
  }

  def authenticateWithToken(token: String): Option[UserJson] = {
    UserJson(userDao.findByToken(token))
  }

  def findByLogin(login: String): Option[UserJson] = {
    UserJson(userDao.findByLowerCasedLogin(login))
  }

  def findByEmail(email: String): Option[UserJson] = {
    UserJson(userDao.findByEmail(email.toLowerCase))
  }

  def isUserDataValid(loginOpt: Option[String], emailOpt: Option[String], passwordOpt: Option[String]): Boolean = {
    registrationDataValidator.isDataValid(loginOpt, emailOpt, passwordOpt)
  }

  def checkUserExistenceFor(userLogin: String, userEmail: String): Either[String, Unit] = {
    var messageEither: Either[String, Unit] = Right(None)

    findByLogin(userLogin) foreach (_ => messageEither = Left("Login already in use!"))
    findByEmail(userEmail) foreach (_ => messageEither = Left("E-mail already in use!"))

    messageEither
  }

  def changeLogin(currentLogin: String, newLogin: String): Either[String, Unit] = {
    findByLogin(newLogin) match {
      case Some(u) => Left("Login is already taken")
      case None => Right(userDao.changeLogin(currentLogin, newLogin))
    }
  }

  def changeEmail(currentEmail: String, newEmail: String): Either[String, Unit] = {
    findByEmail(newEmail) match {
      case Some(u) => Left("E-mail used by another user")
      case None => Right(userDao.changeEmail(currentEmail, newEmail))
    }
  }

  def changePassword(userToken: String, currentPassword: String, newPassword: String): Either[String, Unit] = {
    userDao.findByToken(userToken) match {
      case Some(u) => if (User.passwordsMatch(currentPassword, u)) {
        Right(userDao.changePassword(u.id, User.encryptPassword(newPassword, u.salt)))
      } else {
        Left("Current password is invalid")
      }
      case None => Left("User not found hence cannot change password")
    }
  }

}
