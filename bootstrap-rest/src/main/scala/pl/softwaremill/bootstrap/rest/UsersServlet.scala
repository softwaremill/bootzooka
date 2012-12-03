package pl.softwaremill.bootstrap.rest

import org.scalatra._
import pl.softwaremill.bootstrap.common.JsonWrapper
import pl.softwaremill.bootstrap.service.user.UserService
import pl.softwaremill.bootstrap.service.data.UserJson

class UsersServlet(val userService: UserService) extends JsonServletWithAuthentication with CookieSupport {

  post() {
    val userOpt: Option[UserJson] = authenticate()
    userOpt match {
      case Some(loggedUser) =>
        loggedUser
      case _ =>
        halt(401, "Invalid login and/or password")
    }
  }

  get() {
    haltIfNotAuthenticated()
    user
  }

  get("/logout") {
    if(isAuthenticated) { // call logout only when logged in to avoid NPE
      logOut()
    }
  }

  put("/register") {
    var messageOpt: Option[String] = None

    if(userService.isUserDataValid((parsedBody \ "login").extractOpt[String], (parsedBody \ "email").extractOpt[String],
      (parsedBody \ "password").extractOpt[String]) == false) {
        messageOpt = Some("Wrong user data!")
    }
    else {
      val userLogin = parsedBody \ "login" extractOrElse("")
      val userEmail = parsedBody \ "email" extractOrElse("")

      userService.checkUserExistenceFor(userLogin, userEmail) match {
        case Left(error) => messageOpt = Some(error)
        case _ =>
      }
    }

    messageOpt match {
      case Some(message) => {
        JsonWrapper(message)
      }
      case _ => {
        userService.registerNewUser((parsedBody \ "login").extract[String],
          (parsedBody \ "email").extract[String], (parsedBody \ "password").extract[String])
        JsonWrapper("success")
      }
    }
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

