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

  protected def internalAddUser(user: User) {
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
    users.find(user => user.email.toLowerCase == email.toLowerCase)
  }

  def findByLowerCasedLogin(login: String): Option[User] = {
    users.find(user => user.loginLowerCased == login.toLowerCase)
  }

  def findByLoginOrEmail(loginOrEmail: String): Option[User] = {
    findByEmail(loginOrEmail) match {
      case Some(user) => Option(user)
      case _ => findByLowerCasedLogin(loginOrEmail)
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

  def changePassword(user: User, password: String) {
    val modifiedUser = user.copy(password = password, token = password)
    val userIndex: Int = users.indexOf(user)
    users = users.take(userIndex) ::: List(modifiedUser) ::: users.drop(userIndex + 1)
  }

  def changeLogin(userId: String, login: String) {
    load(userId) match {
      case Some(user) => {
        users = users.updated(users.indexOf(user), user.copy(login = login, loginLowerCased = login.toLowerCase))
      }
      case _ =>
    }
  }

  def changeEmail(userId: String, email: String) {
    load(userId) match {
      case Some(user) => {
        users = users.updated(users.indexOf(user), user.copy(email = email))
      }
      case _ =>
    }
  }
}
