package pl.softwaremill.bootstrap.service.user

import pl.softwaremill.bootstrap.dao.UserDAO
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.common.Utils

class UserService(userDAO: UserDAO, registrationDataValidator: RegistrationDataValidator) {

  def load(userId: Int) = {
    userDAO.load(userId)
  }

  def loadAll = {
    userDAO.loadAll
  }

  def count(): Long = {
    userDAO.count()
  }

  def registerNewUser(user: User) {
    userDAO.add(user)
  }

  def authenticate(login: String, nonEncryptedPassword: String): Option[User] = {
    userDAO.findBy((u: User) => (u.login.equalsIgnoreCase(login) && u.password.equals(Utils.sha256(nonEncryptedPassword, u.login))))
  }

  def authenticateWithToken(token: String): Option[User] = {
    userDAO.findBy((u: User) => u.token.equals(token))
  }

  def findByLogin(login: String): Option[User] = {
    userDAO.findBy((u: User) => u.login.equals(login))
  }

  def findByEmail(email: String): Option[User] = {
    userDAO.findBy((u: User) => u.email.equals(email))
  }

  def isUserDataValid(loginOpt: Option[String], emailOpt: Option[String], passwordOpt: Option[String]): Boolean = {
    registrationDataValidator.isDataValid(loginOpt, emailOpt, passwordOpt)
  }

  def checkUserExistence(user: User): Either[String, Unit] = {
    var messageEither: Either[String, Unit] = Right(None)

    findByLogin(user.login) foreach( _ => messageEither = Left("Login already in use!"))
    findByEmail(user.email) foreach( _ => messageEither = Left("E-mail already in use!"))

    messageEither
  }

}
