package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.service.PasswordRecoveryService
import org.apache.commons.lang3.StringUtils
import com.softwaremill.bootzooka.common.StringJsonWrapper
import com.softwaremill.bootzooka.service.user.UserService
import org.scalatra.NoContent
import org.scalatra.swagger.{StringResponseMessage, SwaggerSupport, Swagger}

/**
 * Servlet handling requests related to password recovery.
 */
class PasswordRecoveryServlet(passwordRecoveryService: PasswordRecoveryService, userService: UserService)(override implicit val swagger: Swagger)
  extends JsonServlet with SwaggerMappable with PasswordRecoveryServletApiDocs {

  override def mappingPath = PasswordRecoveryServlet.MappingPath

  post("/", operation(requestPasswordReset)) {
    val login = (parsedBody \ "login").extractOpt[String].getOrElse("")

    userService.checkUserExistenceFor(login, login) match {
      case Right(_) => haltWithNotFound("No user with given login/e-mail found.")
      case _ =>
        passwordRecoveryService.sendResetCodeToUser(login)
        StringJsonWrapper("success")
    }
  }

  post("/:code", operation(resetPassword)) {
    val code = params("code")
    val password = (parsedBody \ "password").extractOpt[String].getOrElse("")
    if (!StringUtils.isBlank(password)) {
      passwordRecoveryService.performPasswordReset(code, password) match {
        case Left(e) => haltWithForbidden(e)
        case _ => NoContent()
      }
    } else {
      haltWithBadRequest("missingpassword")
    }
  }
}

object PasswordRecoveryServlet {
  val MappingPath = "passwordrecovery"
}

trait PasswordRecoveryServletApiDocs extends SwaggerSupport {
  self: PasswordRecoveryServlet =>

  override protected val applicationDescription = "Password recovery"

  val requestPasswordReset = (
    apiOperation[StringJsonWrapper]("requestPasswordReset")
      summary "Request password reset"
      parameter bodyParam[PasswordResetRequestCommand]("body").description("User login").required
      responseMessages(
        StringResponseMessage(200, "OK"),
        StringResponseMessage(404, "No user with given login/e-mail found")
      )
    )

  val resetPassword = (
    apiOperation[Unit]("resetPassword")
      summary "Reset password"
      parameters(
        pathParam[String]("code").description("Password reset code").required,
        bodyParam[PasswordResetCommand]("body").description("New password").required
      )
      responseMessages(
        StringResponseMessage(200, "OK"),
        StringResponseMessage(400, "Missing password"),
        StringResponseMessage(403, "Invalid password reset code")
      )
    )
}

case class PasswordResetRequestCommand(login: String)

case class PasswordResetCommand(password: String)
