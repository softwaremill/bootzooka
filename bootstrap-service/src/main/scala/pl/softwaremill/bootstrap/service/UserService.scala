package pl.softwaremill.bootstrap.service

import pl.softwaremill.bootstrap.dao.UserDAO
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.common.Utils

class UserService(userDAO: UserDAO) {

  def loadAll = {
    userDAO.loadAll
  }

  def count(): Long = {
    userDAO.count()
  }

  def registerNewUser(user: User) {
    userDAO.add(user)
  }

  def authenticate(loginOrEmail: String, nonEncryptedPassword: String): User = {
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(loginOrEmail)

    userOpt match {
      case Some(user) => {
        if(user.password.equals(Utils.sha256(nonEncryptedPassword, user.login))) {
          user
        }
      }
    }

    throw new Exception("Invalid login and/or password");
  }

}
