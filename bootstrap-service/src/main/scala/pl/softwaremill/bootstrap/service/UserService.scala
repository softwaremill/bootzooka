package pl.softwaremill.bootstrap.service

import pl.softwaremill.bootstrap.dao.UserDAO
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.common.Utils

class UserService(userDAO: UserDAO) {

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

}
