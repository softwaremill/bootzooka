package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.service.PasswordRecoveryService
import org.apache.commons.lang3.StringUtils
import com.softwaremill.bootzooka.common.StringJsonWrapper
import com.softwaremill.bootzooka.service.user.UserService
import org.scalatra.NoContent

/**
 * Servlet handling requests related to password recovery.
 */
class PasswordRecoveryServlet(passwordRecoveryService: PasswordRecoveryService, userService: UserService) extends JsonServlet {

  post("/") {
    val login = (parsedBody \ "login").extractOpt[String].getOrElse("")

    userService.checkUserExistenceFor(login, login) match {
      case Right(_) => haltWithNotFound("No user with given login/e-mail found.")
      case _ =>
        passwordRecoveryService.sendResetCodeToUser(login)
        StringJsonWrapper("success")
    }
  }

  post("/:code") {
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
