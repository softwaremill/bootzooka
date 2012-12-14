package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.User

trait UserDAO {

  def loadAll: List[User]

  def countItems(): Long

  def add(user: User)

  def remove(userId: String)

  def load(userId: String): Option[User]

  def findByEmail(email: String): Option[User]

  def findByLowerCasedLogin(login: String): Option[User]

  def findByLoginOrEmail(loginOrEmail: String): Option[User]

  def findByToken(token: String): Option[User]

  def findByLoginAndEncryptedPassword(login: String, encryptedPassword: String): Option[User]

}
