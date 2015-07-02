package com.softwaremill.bootzooka.api

import com.softwaremill.bootzooka.common.StringJsonWrapper
import com.softwaremill.bootzooka.service.data.UserJson
import com.softwaremill.bootzooka.service.user.UserService
import org.scalatra._
import org.scalatra.swagger.{StringResponseMessage, Swagger, SwaggerSupport}

import scala.concurrent.{ExecutionContext, Future}

class UsersServlet(val userService: UserService)(override implicit val swagger: Swagger, ec: ExecutionContext)
    extends JsonServletWithAuthentication with SwaggerMappable with UsersServlet.ApiDocs with FutureSupport {

  override def mappingPath = UsersServlet.MappingPath
  override protected implicit def executor = ec

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
    }
    else {
      val paramLogin = login
      val paramPass = password
      val paramEmail = email // these values have to be extracted before
      new AsyncResult {
        val is = userService.checkUserExistenceFor(paramLogin, paramEmail).flatMap {
          case Left(error) =>
            Future { haltWithConflict(error) }
          case _ =>
            val loginEscaped = scala.xml.Utility.escape(paramLogin)
            userService.registerNewUser(loginEscaped, paramEmail, paramPass).map(
              _ => Created(StringJsonWrapper("success"))
            )
        }
      }
    }

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

    val updateFut = if (!login.isEmpty) {
      changeLogin()
    }
    else if (!email.isEmpty) {
      changeEmail()
    }
    else Future.failed(new IllegalStateException("You have to provide new login or email"))
    new AsyncResult {
      val is = updateFut.map { errorMsgOpt =>
        errorMsgOpt.foreach(msg => haltWithConflict(msg))
        NoContent()
      }
    }
  }

  private def changeLogin(): Future[Option[String]] = {
    logger.debug(s"Updating login: ${user.login} -> $login")
    userService.changeLogin(user.login, login).map {
      case Left(error) => Some(error)
      case _ => None
    }
  }

  private def changeEmail(): Future[Option[String]] = {
    logger.debug(s"Updating email: ${user.email} -> $email")
    userService.changeEmail(user.email, email.toLowerCase).map {
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
    }
    else if (newPassword.isEmpty) {
      haltWithBadRequest("Parameter newPassword is missing")
    }
    val token = user.token
    new AsyncResult() {
      val is = changePassword(token, currentPassword, newPassword).map {
        case Some(message) => haltWithForbidden(message)
        case None => NoContent()
      }
    }
  }

  def changePassword(token: String, currentPassword: String, newPassword: String): Future[Option[String]] = {
    userService.changePassword(token, currentPassword, newPassword).map {
      case Left(error) => Some(error)
      case _ => None
    }
  }
}

object UsersServlet {
  val MappingPath = "users"

  // only enclosing object's companions have access to this trait
  protected trait ApiDocs extends SwaggerSupport {
    self: UsersServlet =>

    override protected val applicationDescription = "User management and authentication"

    protected val authenticate = (
      apiOperation[UserJson]("authenticate")
      summary "Authenticate user"
      parameter bodyParam[AuthenticationCommand]("body").description("Authentication data").required
      responseMessages (
        StringResponseMessage(200, "OK"),
        StringResponseMessage(401, "Invalid login and/or password")
      )
    )

    protected val getAuthenticatedUser = (
      apiOperation[UserJson]("getAuthenticatedUser")
      summary "Get authenticated user"
      responseMessages (
        StringResponseMessage(200, "OK"),
        StringResponseMessage(401, "User not logged in")
      )
    )

    protected val logout = (
      apiOperation[Unit]("logout")
      summary "Log out authenticated user"
      responseMessage StringResponseMessage(204, "OK")
    )

    protected val register = (
      apiOperation[StringJsonWrapper]("register")
      summary "Register new user"
      parameter bodyParam[RegistrationCommand]("body").description("Registration data").required
      responseMessages (
        StringResponseMessage(201, "Created"),
        StringResponseMessage(400, "Wrong user data"),
        StringResponseMessage(409, "Login or e-mail already exists")
      )
    )

    protected val update = (
      apiOperation[Unit]("update")
      summary "Update user profile"
      parameter bodyParam[UserUpdateCommand]("body").description("Fields to update")
      responseMessages (
        StringResponseMessage(204, "OK"),
        StringResponseMessage(401, "User not logged in"),
        StringResponseMessage(409, "Login or e-mail already exists")
      )
    )

    protected val changePassword = (
      apiOperation[Unit]("changePassword")
      summary "Change password"
      parameter bodyParam[PasswordChangeCommand]("body").description("Current and new password").required
      responseMessages (
        StringResponseMessage(204, "OK"),
        StringResponseMessage(400, "Current or new password is missing"),
        StringResponseMessage(401, "User not logged in"),
        StringResponseMessage(403, "Current password is invalid")
      )
    )

    protected val getAll = (
      apiOperation[List[UserJson]]("getAll")
      summary "Get all users"
      responseMessages (
        StringResponseMessage(200, "OK"),
        StringResponseMessage(401, "User not logged in")
      )
    )
  }

  private[this] case class AuthenticationCommand(login: String, password: String, rememberMe: Boolean)

  private[this] case class RegistrationCommand(login: String, email: String, password: String)

  private[this] case class UserUpdateCommand(login: String, email: String)

  private[this] case class PasswordChangeCommand(currentPassword: String, newPassword: String)

}
