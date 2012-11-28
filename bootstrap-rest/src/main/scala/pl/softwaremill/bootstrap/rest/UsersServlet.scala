package pl.softwaremill.bootstrap.rest

import org.scalatra._
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.common.JsonWrapper
import validators.UserExistenceChecker
import pl.softwaremill.bootstrap.service.user.{RegistrationDataValidator, UserService}

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
    var messageOpt: Option[String] = None

    if(registrationDataValidator.isDataValid((parsedBody \ "login").extractOpt[String], (parsedBody \ "email").extractOpt[String],
      (parsedBody \ "password").extractOpt[String]) == false) {
        messageOpt = Some("Wrong user data!")
    }
    else {
      val newUser = parsedBody.extract[User]

      userExistenceChecker.check(newUser) match {
        case Left(error) => messageOpt = Some(error)
        case _ =>
      }
    }

    messageOpt match {
      case Some(message) => {
        JsonWrapper(message)
      }
      case _ => {
        userService.registerNewUser(parsedBody.extract[User])
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

