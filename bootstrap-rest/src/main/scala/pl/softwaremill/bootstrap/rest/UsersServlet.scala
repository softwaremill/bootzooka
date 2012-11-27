package pl.softwaremill.bootstrap.rest

import org.scalatra._
import pl.softwaremill.bootstrap.service.UserService
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.common.JsonWrapper

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

  put("/register") {
    val user = parsedBody.extract[User]
    var message = ""
    userService.findByLogin(user.login) match {
      case Some(u) => message = "Login already in use!"
      case _ =>
    }

    userService.findByEmail(user.email) match {
      case Some(u) => message = "E-mail already in use!"
      case _ =>
    }

    if (message.isEmpty) {
      if (user.valid()) {
        userService.registerNewUser(user)
        message = "success"
      } else {
        message = "Wrong user data!"
      }
    }
    JsonWrapper(message)
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

