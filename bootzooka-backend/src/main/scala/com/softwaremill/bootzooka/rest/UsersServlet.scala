package com.softwaremill.bootzooka.rest

import org.scalatra._
import com.softwaremill.bootzooka.common.StringJsonWrapper
import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.service.data.UserJson
import org.apache.commons.lang3.StringEscapeUtils._
import org.scalatra.swagger.{StringResponseMessage, SwaggerSupport, Swagger}

class UsersServlet(val userService: UserService)(override implicit val swagger: Swagger)
  extends JsonServletWithAuthentication with SwaggerMappable with UsersServletApiDocs {

  override def mappingPath = UsersServlet.MappingPath

  post("/", operation(authenticate)) {
    val userOpt: Option[UserJson] = authenticate()
    userOpt match {
      case Some(loggedUser) => loggedUser
      case _ => haltWithUnauthorized("Invalid login and/or password")
    }
  }

  get("/", operation(getAuthenticatedUser)) {
    haltIfNotAuthenticated()
    user
  }

  get("/logout", operation(logout)) {
    if (isAuthenticated) {
      // call logout only when logged in to avoid NPE
      logOut()
    }
    NoContent()
  }

  post("/register", operation(register)) {
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

  patch("/", operation(update)) {
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

  post("/changepassword", operation(changePassword)) {
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

  get("/all", operation(getAll)) {
    haltIfNotAuthenticated()
    userService.loadAll
  }
}

object UsersServlet {
  val MappingPath = "users"
}

trait UsersServletApiDocs extends SwaggerSupport {
  self: UsersServlet =>
  
  override protected val applicationDescription = "User management and authentication"

  val authenticate = (
    apiOperation[UserJson]("authenticate")
      summary "Authenticate user"
      parameter bodyParam[AuthenticationCommand]("body").description("Authentication data").required
      responseMessages(
        StringResponseMessage(200, "OK"),
        StringResponseMessage(401, "Invalid login and/or password")
      )
    )

  val getAuthenticatedUser = (
    apiOperation[UserJson]("getAuthenticatedUser")
      summary "Get authenticated user"
      responseMessages(
        StringResponseMessage(200, "OK"),
        StringResponseMessage(401, "User not logged in")
      )
    )

  val logout = (
    apiOperation[Unit]("logout")
      summary "Log out authenticated user"
      responseMessage StringResponseMessage(204, "OK")
    )

  val register = (
    apiOperation[StringJsonWrapper]("register")
      summary "Register new user"
      parameter bodyParam[RegistrationCommand]("body").description("Registration data").required
      responseMessages(
        StringResponseMessage(201, "Created"),
        StringResponseMessage(400, "Wrong user data"),
        StringResponseMessage(409, "Login or e-mail already exists")
      )
    )

  val update = (
    apiOperation[Unit]("update")
      summary "Update user profile"
      parameter bodyParam[UserUpdateCommand]("body").description("Fields to update")
      responseMessages(
        StringResponseMessage(204, "OK"),
        StringResponseMessage(409, "Login or e-mail already exists")
      )
    )

  val changePassword = (
    apiOperation[Unit]("changePassword")
      summary "Change password"
      parameter bodyParam[PasswordChangeCommand]("body").description("Current and new password").required
      responseMessages(
        StringResponseMessage(204, "OK"),
        StringResponseMessage(400, "Current or new password is missing"),
        StringResponseMessage(403, "Current password is invalid")
      )
    )

  val getAll = (
    apiOperation[List[UserJson]]("getAll")
      summary "Get all users"
      responseMessages(
        StringResponseMessage(200, "OK"),
        StringResponseMessage(401, "User not logged in")
      )
    )
}

case class AuthenticationCommand(login: String, password: String, rememberMe: Boolean)

case class RegistrationCommand(login: String, email: String, password: String)

case class UserUpdateCommand(login: String, email: String)

case class PasswordChangeCommand(currentPassword: String, newPassword: String)