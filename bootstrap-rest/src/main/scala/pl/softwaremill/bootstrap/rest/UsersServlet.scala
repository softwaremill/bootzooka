package pl.softwaremill.bootstrap.rest

import org.scalatra._
import pl.softwaremill.bootstrap.auth.AuthenticationSupport
import pl.softwaremill.bootstrap.service.UserService
import pl.softwaremill.bootstrap.domain.User

class UsersServlet(val userService: UserService) extends JsonServletWithAuthentication with CookieSupport {

  post() {
    val userOpt: Option[User] = authenticate()
    userOpt match {
      case Some(user) =>
        user
      case _ =>
        halt(401, "Invalid login and/or password")
    }
  }

  get() {
    haltIfNotAuthenticated()
    user
  }

  get("/logout") {
    logOut()
  }

  post("/regiter") {
    println("Registering new user stub")
  }

  override def login: String = {
    (parsedBody \ "login").extractOpt[String].getOrElse("")
  }

  override def password: String = {
    (parsedBody \ "password").extractOpt[String].getOrElse("")
  }

  override def rememberMe: Boolean = {
    (parsedBody \ "rememberme").extractOpt[Boolean].getOrElse(false)
  }

}

