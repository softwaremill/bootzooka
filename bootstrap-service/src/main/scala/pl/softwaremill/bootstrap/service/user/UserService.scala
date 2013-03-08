package pl.softwaremill.bootstrap.service.user

import pl.softwaremill.bootstrap.dao.UserDAO
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.service.data.UserJson
import pl.softwaremill.bootstrap.service.schedulers.EmailScheduler
import pl.softwaremill.bootstrap.service.templates.EmailTemplatingEngine
import pl.softwaremill.common.util.RichString
import java.util.UUID

class UserService(userDAO: UserDAO, registrationDataValidator: RegistrationDataValidator, emailScheduler: EmailScheduler,
                  emailTemplatingEngine: EmailTemplatingEngine) {

  def load(userId: String) = {
    UserJson(userDAO.load(userId))
  }

  def loadAll = {
    UserJson(userDAO.loadAll)
  }

  def count(): Long = {
    userDAO.countItems()
  }

  def registerNewUser(login: String, email: String, password: String) {
    val salt = RichString.generateRandom(16)
    val token = UUID.randomUUID().toString
    userDAO.add(User(login, email.toLowerCase, password, salt, token))
    val confirmationEmail = emailTemplatingEngine.registrationConfirmation(login)
    emailScheduler.scheduleEmail(email, confirmationEmail)
  }

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)
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
    UserJson(userDAO.findByToken(token))
  }

  def findByLogin(login: String): Option[UserJson] = {
    UserJson(userDAO.findByLowerCasedLogin(login))
  }

  def findByEmail(email: String): Option[UserJson] = {
    UserJson(userDAO.findByEmail(email.toLowerCase))
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
      case None => Right(userDAO.changeLogin(currentLogin, newLogin))
    }
  }

  def changeEmail(currentEmail: String, newEmail: String): Either[String, Unit] = {
    findByEmail(newEmail) match {
      case Some(u) => Left("E-mail used by another user")
      case None => Right(userDAO.changeEmail(currentEmail, newEmail))
    }
  }

  def changePassword(userToken: String, currentPassword: String, newPassword: String): Either[String, Unit] = {
    userDAO.findByToken(userToken) match {
      case Some(u) => if (User.passwordsMatch(currentPassword, u)) {
        Right(userDAO.changePassword(u.id.toString, User.encryptPassword(newPassword, u.salt)))
      } else {
        Left("Current password is invalid")
      }
      case None => Left("User not found hence cannot change password")
    }
  }

}
