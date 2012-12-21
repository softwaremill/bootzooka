package pl.softwaremill.bootstrap.service.user

import pl.softwaremill.bootstrap.dao.UserDAO
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.common.Utils
import pl.softwaremill.bootstrap.service.data.UserJson
import pl.softwaremill.bootstrap.service.schedulers.EmailScheduler

class UserService(userDAO: UserDAO, registrationDataValidator: RegistrationDataValidator, emailScheduler: EmailScheduler) {

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
    userDAO.add(User(login, email.toLowerCase, Utils.sha256(password, login.toLowerCase),
      Utils.sha256(password, login.toLowerCase)))

    emailScheduler.scheduleEmail(email, "SML Bootstrap - registration confirmation", "Dear " + login +
      "\n\nThank you for registering in our application.\n\n-- \nRegards,\nSoftwareMill Bootstrap Dev Team\nhttp://SoftwareMill.com")
  }

  def authenticate(login: String, nonEncryptedPassword: String): Option[UserJson] = {
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)
    userOpt match {
      case Some(u) => {
        if (u.password.equals(Utils.sha256(nonEncryptedPassword, u.login.toLowerCase))) {
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

}
