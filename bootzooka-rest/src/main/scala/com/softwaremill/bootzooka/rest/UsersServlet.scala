package com.softwaremill.bootzooka.rest

import org.scalatra._
import com.softwaremill.bootzooka.common.JsonWrapper
import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.service.data.UserJson
import org.apache.commons.lang3.StringEscapeUtils._

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

  get("/all") {
    haltIfNotAuthenticated()
    userService.loadAll
  }
}

object UsersServlet {
  val MAPPING_PATH = "users"
}

