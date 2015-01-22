package com.softwaremill.bootzooka.dao.user

import com.softwaremill.bootzooka.domain.User

class InMemoryUserDAO extends UserDAO {

  var users = List[User]()

  def loadAll: List[User] = {
    users
  }

  override def findForIdentifiers(uniqueIds: Set[UserId]): List[User] = {
    users.filter(user => uniqueIds.contains(user.id))
  }

  def countItems(): Long = {
    users.size
  }

  protected def internalAddUser(user: User) {
    users ::= user
  }

  def remove(userId: UserId) {
    load(userId) foreach { user =>
      users = users.diff(List(user))
    }
  }

  def load(userId: UserId): Option[User] = {
    users.find(_.id == userId)
  }

  def findByEmail(email: String): Option[User] = {
    users.find(user => user.email.toLowerCase == email.toLowerCase)
  }

  def findByLowerCasedLogin(login: String): Option[User] = {
    users.find(user => user.loginLowerCased == login.toLowerCase)
  }

  def findByLoginOrEmail(loginOrEmail: String): Option[User] = {
    findByEmail(loginOrEmail) orElse findByLowerCasedLogin(loginOrEmail)
  }

  def findByToken(token: String): Option[User] = {
    users.find(user => user.token == token)
  }

  def changePassword(userId: UserId, password: String) {
    load(userId) foreach { user =>
      users = users.updated(users.indexOf(user), user.copy(password = password))
    }
  }

  def changeLogin(currentLogin: String, newLogin: String) {
    findByLowerCasedLogin(currentLogin) foreach { user =>
      users = users.updated(users.indexOf(user), user.copy(login = newLogin, loginLowerCased = newLogin.toLowerCase))
    }
  }

  def changeEmail(currentEmail: String, newEmail: String) {
    findByEmail(currentEmail) foreach { user =>
      users = users.updated(users.indexOf(user), user.copy(email = newEmail))
    }
  }
}
