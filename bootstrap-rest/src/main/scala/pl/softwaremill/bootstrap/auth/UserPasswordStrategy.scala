
package pl.softwaremill.bootstrap.auth

import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.service.user.UserService

class UserPasswordStrategy(protected val app: ScalatraBase, login: String, password: String, val userService: UserService) extends ScentryStrategy[User] {

  override def name: String = UserPassword.name

  override def isValid = {
    !login.isEmpty && !password.isEmpty
  }

  override def authenticate() = {
   userService.authenticate(login, password)
  }

}

object UserPassword {

  val name = "UserPassword"

}
