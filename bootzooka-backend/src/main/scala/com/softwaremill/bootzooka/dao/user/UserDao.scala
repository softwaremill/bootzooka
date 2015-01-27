package com.softwaremill.bootzooka.dao.user

import java.util.UUID

import com.softwaremill.bootzooka.domain.User

trait UserDao {

  type UserId = UUID

  def loadAll: List[User]

  def countItems(): Long

  def add(user: User): Unit = {
    if (findByLowerCasedLogin(user.login).isDefined || findByEmail(user.email).isDefined) {
      throw new Exception("User with given e-mail or login already exists")
    }
    internalAddUser(user)
  }

  protected def internalAddUser(user: User): Unit

  def remove(userId: UserId): Unit

  def load(userId: UserId): Option[User]

  def findByEmail(email: String): Option[User]

  def findByLowerCasedLogin(login: String): Option[User]

  def findByLoginOrEmail(loginOrEmail: String): Option[User]

  def findForIdentifiers(uniqueIds: Set[UserId]): List[User]

  def findByToken(token: String): Option[User]

  def changePassword(userId: UserId, password: String): Unit

  def changeLogin(currentLogin: String, newLogin: String): Unit

  def changeEmail(currentEmail: String, newEmail: String): Unit

}
