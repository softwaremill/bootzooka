package com.softwaremill.bootzooka.rest

import org.scalatra._
import com.softwaremill.bootzooka.common.JsonWrapper
import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.service.data.UserJson
import org.apache.commons.lang3.StringEscapeUtils._
import org.scalatra.swagger.{Swagger, SwaggerSupport}

class UsersServlet(val userService: UserService, val swagger: Swagger) extends JsonServletWithAuthentication with UsersServletSwaggerDefinition with CookieSupport {

  post(operation(loginOperation)) {
    val userOpt: Option[UserJson] = authenticate()
    userOpt match {
      case Some(loggedUser) =>
        loggedUser
      case _ =>
        halt(401, "Invalid login and/or password")
    }
  }

  get(operation(userProfileOperation)) {
    haltIfNotAuthenticated()
    user
  }

  get("/logout", operation(logoutOperation)) {
    if (isAuthenticated) {
      // call logout only when logged in to avoid NPE
      logOut()
    }
  }

  post("/register") {
    var messageOpt: Option[String] = None

    if (userService.isUserDataValid((parsedBody \ "login").extractOpt[String], (parsedBody \ "email").extractOpt[String],
      (parsedBody \ "password").extractOpt[String]) == false) {
      messageOpt = Some("Wrong user data!")
    } else {
      val userLogin = parsedBody \ "login" extractOrElse ("")
      val userEmail = parsedBody \ "email" extractOrElse ("")

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
        userService.registerNewUser(escapeHtml4((parsedBody \ "login").extract[String]),
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

  def email: String = {
    (parsedBody \ "email").extractOpt[String].getOrElse("")
  }

  patch("/") {
    haltIfNotAuthenticated()
    logger.debug("Updating user profile")
    var messageOpt: Option[String] = None

    if (!login.isEmpty) {
      messageOpt = changeLogin()
    }

    if (!email.isEmpty) {
      messageOpt = changeEmail()
    }

    messageOpt match {
      case Some(message) => halt(403, JsonWrapper(message))
      case None => Ok
    }
  }

  private def changeLogin(): Option[String] = {
    logger.debug(s"Updating login: ${user.login} -> ${login}")
    userService.changeLogin(user.login, login) match {
      case Left(error) => Some(error)
      case _ => None
    }
  }

  private def changeEmail(): Option[String] = {
    logger.debug(s"Updating email: ${user.email} -> ${email}")
    userService.changeEmail(user.email, email.toLowerCase) match {
      case Left(error) => Some(error)
      case _ => None
    }
  }

  post("/changepassword") {
    haltIfNotAuthenticated()
    val currentPassword = (parsedBody \ "currentPassword").extractOpt[String].getOrElse("")
    val newPassword = (parsedBody \ "newPassword").extractOpt[String].getOrElse("")
    var messageOpt: Option[String] = None
    if (currentPassword.isEmpty) {
      messageOpt = Some("Parameter currentPassword is missing")
    } else if (newPassword.isEmpty) {
      messageOpt = Some("Parameter newPassword is missing")
    } else {
      messageOpt = changePassword(currentPassword, newPassword)
    }

    messageOpt match {
      case Some(message) => halt(403, JsonWrapper(message))
      case None => Ok
    }
  }

  def changePassword(currentPassword: String, newPassword: String): Option[String] = {
    userService.changePassword(user.token, currentPassword, newPassword) match {
      case Left(error) => Some(error)
      case _ => None
    }
  }

}

object UsersServlet {
  val MAPPING_PATH = "users"
}

trait UsersServletSwaggerDefinition extends SwaggerSupport {

  override protected val applicationName = Some(UsersServlet.MAPPING_PATH)
  protected val applicationDescription: String = "User and session management endpoint"

  val loginOperation = apiOperation[UserJson]("login")
    .summary("log user in")
    .parameter(bodyParam[String]("login").description("user login").required)
    .parameter(bodyParam[String]("password").description("user password").required)
    .parameter(bodyParam[String]("rememberme").description("whether user session should be remembered").required)

  val userProfileOperation = apiOperation[UserJson]("userProfile")
    .summary("gets logged in user")
    .notes("Requires user to be authenticated")

  val logoutOperation = apiOperation[Unit]("userProfile")
    .summary("logs user out")
    .notes("Requires user to be authenticated")

}

