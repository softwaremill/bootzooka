package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.domain.User
import java.util.UUID
import org.bson.types.ObjectId

trait UserDAO {

  def loadAll: List[User]

  def countItems(): Long

  def add(user: User) {
    if (findByLowerCasedLogin(user.login).isDefined || findByEmail(user.email).isDefined) {
      throw new Exception("User with given e-mail or login already exists")
    }
    internalAddUser(user)
  }

  protected def internalAddUser(user: User)

  def remove(userId: String)

  def load(userId: String): Option[User]

  def findByEmail(email: String): Option[User]

  def findByLowerCasedLogin(login: String): Option[User]

  def findByLoginOrEmail(loginOrEmail: String): Option[User]

  def findForIdentifiers(ids: List[ObjectId]): List[User]

  def findByToken(token: String): Option[User]

  def changePassword(userId:String, password: String)

  def changeLogin(currentLogin: String, newLogin: String)

  def changeEmail(currentEmail: String, newEmail: String)

}
