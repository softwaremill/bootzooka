
package pl.softwaremill.bootstrap.auth

import org.scalatra.ScalatraBase
import org.scalatra.auth.ScentryStrategy

class UserPasswordStrategy(protected val app: ScalatraBase, login: String, password: String) extends ScentryStrategy[User] {

  override def name: String = UserPassword.name

  override def isValid = {
    !login.isEmpty && !password.isEmpty
  }

  override def authenticate() = {
    val userOpt: Option[User] = Users.list.find {
      user =>
        user.login == login && user.password == password
    }
    userOpt
  }

}

object UserPassword {

  val name = "UserPassword"

}
