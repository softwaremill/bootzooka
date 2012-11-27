package pl.softwaremill.bootstrap.rest

import org.scalatra._
import pl.softwaremill.bootstrap.auth.AuthenticationSupport
import pl.softwaremill.bootstrap.service.UserService
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.common.JsonWrapper
import validators.{UserExistenceChecker, RegistrationDataValidator}

class UsersServlet(val userService: UserService) extends JsonServletWithAuthentication with CookieSupport {

  val registrationDataValidator: RegistrationDataValidator = new RegistrationDataValidator()
  val userExistenceChecker: UserExistenceChecker = new UserExistenceChecker(userService)

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
    var message = ""

    if(registrationDataValidator.isDataValid((parsedBody \ "login").extractOpt[String], (parsedBody \ "email").extractOpt[String],
      (parsedBody \ "password").extractOpt[String]) == false) {
        message = "Wrong user data!"
    }
    else {
      val newUser = parsedBody.extract[User]
      message = userExistenceChecker.check(newUser).getOrElse("")
    }

    if (message.isEmpty) {
        userService.registerNewUser(parsedBody.extract[User])
        message = "success"
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

