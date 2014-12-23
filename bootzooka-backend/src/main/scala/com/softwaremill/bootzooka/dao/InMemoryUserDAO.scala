package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.domain.User
import org.bson.types.ObjectId

class InMemoryUserDAO extends UserDAO {

  var users = List[User]()

  def loadAll: List[User] = {
    users
  }

  override def findForIdentifiers(ids: List[ObjectId]): List[User] =
    ids.distinct.map(id => users.find(user => user.id == id.toString).get);

  def countItems(): Long = {
    users.size
  }

  protected def internalAddUser(user: User) {
    users ::= user
  }

  def remove(userId: String) {
    load(userId) match {
      case Some(user) => users = users.diff(List(user))
      case _ =>
    }
  }

  def load(userId: String): Option[User] = {
    users.find(user => user.id == userId)
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
    users.find(user => user.token == token)
  }

  def changePassword(userId: String, password: String) {
    load(userId) match {
      case Some(u) => users = users.updated(users.indexOf(u), u.copy(password = password))
      case None =>
    }
  }

  def changeLogin(currentLogin: String, newLogin: String) {
    findByLowerCasedLogin(currentLogin) match {
      case Some(user) => {
        users = users.updated(users.indexOf(user), user.copy(login = newLogin, loginLowerCased = newLogin.toLowerCase))
      }
      case _ =>
    }
  }

  def changeEmail(currentEmail: String, newEmail: String) {
    findByEmail(currentEmail) match {
      case Some(user) => {
        users = users.updated(users.indexOf(user), user.copy(email = newEmail))
      }
      case _ =>
    }
  }
}
