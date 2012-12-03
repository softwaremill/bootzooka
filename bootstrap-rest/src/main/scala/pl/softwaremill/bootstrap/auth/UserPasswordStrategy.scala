
package pl.softwaremill.bootstrap.auth

import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy
import pl.softwaremill.bootstrap.service.user.UserService
import pl.softwaremill.bootstrap.service.data.UserJson

class UserPasswordStrategy(protected val app: ScalatraBase, login: String, password: String, val userService: UserService) extends ScentryStrategy[UserJson] {

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
