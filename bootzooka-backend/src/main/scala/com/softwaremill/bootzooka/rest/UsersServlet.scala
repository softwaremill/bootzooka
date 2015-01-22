package com.softwaremill.bootzooka.rest

import org.scalatra._
import com.softwaremill.bootzooka.common.StringJsonWrapper
import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.service.data.UserJson
import org.apache.commons.lang3.StringEscapeUtils._

class UsersServlet(val userService: UserService) extends JsonServletWithAuthentication {

  post() {
    val userOpt: Option[UserJson] = authenticate()
    userOpt match {
      case Some(loggedUser) => loggedUser
      case _ => haltWithUnauthorized("Invalid login and/or password")
    }
  }

  get() {
    haltIfNotAuthenticated()
    user
  }

  get("/logout") {
    if (isAuthenticated) {
      // call logout only when logged in to avoid NPE
      logOut()
    }
    NoContent()
  }

  post("/register") {
    if (!userService.isUserDataValid(loginOpt, emailOpt, passwordOpt)) {
      haltWithBadRequest("Wrong user data!")
    } else {
      userService.checkUserExistenceFor(login, email) match {
        case Left(error) => haltWithConflict(error)
        case _ =>
      }
    }

    userService.registerNewUser(escapeHtml4(login), email, password)

    Created(StringJsonWrapper("success"))
  }

  private def valueOrEmptyString(maybeString: Option[String]) = maybeString.getOrElse("")

  private def loginOpt: Option[String] = (parsedBody \ "login").extractOpt[String]

  override def login: String = valueOrEmptyString(loginOpt)

  private def passwordOpt: Option[String] = (parsedBody \ "password").extractOpt[String]

  override def password: String = valueOrEmptyString(passwordOpt)

  override def rememberMe: Boolean = {
    (parsedBody \ "rememberMe").extractOpt[Boolean].getOrElse(false)
  }

  private def emailOpt: Option[String] = (parsedBody \ "email").extractOpt[String]

  def email: String = valueOrEmptyString(emailOpt)

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
      case Some(message) => haltWithConflict(message)
      case None => NoContent()
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

    if (currentPassword.isEmpty) {
      haltWithBadRequest("Parameter currentPassword is missing")
    } else if (newPassword.isEmpty) {
      haltWithBadRequest("Parameter newPassword is missing")
    }

    changePassword(currentPassword, newPassword) match {
      case Some(message) => haltWithForbidden(message)
      case None => NoContent()
    }
  }

  def changePassword(currentPassword: String, newPassword: String): Option[String] = {
    userService.changePassword(user.token, currentPassword, newPassword) match {
      case Left(error) => Some(error)
      case _ => None
    }
  }

  get("/all") {
    haltIfNotAuthenticated()
    userService.loadAll
  }
}

object UsersServlet {
  val MAPPING_PATH = "users"
}

