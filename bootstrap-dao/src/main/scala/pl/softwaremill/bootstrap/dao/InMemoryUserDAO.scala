package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.common.Utils

class InMemoryUserDAO extends UserDAO {

  var users = List[User]()

  def loadAll: List[User] = {
    users
  }

  def countItems(): Long = {
    users.size
  }

  def add(user: User) {
    if (findByLowerCasedLogin(user.login).isDefined || findByEmail(user.email).isDefined) {
      throw new Exception("User with given e-mail or login already exists")
    }
    users ::= user
  }

  def remove(userId: String) {
    load(userId) match {
      case Some(user) => users.diff(List(user))
      case _ =>
    }
  }

  def load(userId: String): Option[User] = {
    users.find(user => user._id == userId)
  }

  def findByEmail(email: String): Option[User] = {
    users.find(user => user.email == email.toLowerCase)
  }

  def findByLowerCasedLogin(login: String): Option[User] = {
    users.find(user => user.loginLowerCased == login.toLowerCase)
  }

  def findByLoginOrEmail(loginOrEmail: String): Option[User] = {
    users.find(user => user.email == loginOrEmail.toLowerCase) match {
      case Some(user) => Option(user)
      case _ => users.find(user => user.loginLowerCased == loginOrEmail.toLowerCase)
    }
  }

  def findByToken(token: String): Option[User] = {
    users.find(user => user.token == Utils.sha256(user.password, token))
  }

  def findByLoginAndEncryptedPassword(login: String, encryptedPassword: String): Option[User] = {
    users.find(user => user.loginLowerCased == login.toLowerCase) match {
      case Some(user) => user.password == encryptedPassword match {
        case true => Some(user)
      }
      case _ => None
    }
  }
}
