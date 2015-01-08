package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.service.PasswordRecoveryService
import org.apache.commons.lang3.StringUtils
import com.softwaremill.bootzooka.common.JsonWrapper
import com.softwaremill.bootzooka.service.user.UserService

/**
 * Servlet handling requests related to password recovery.
 */
class PasswordRecoveryServlet(passwordRecoveryService: PasswordRecoveryService, userService: UserService) extends JsonServlet {

  post("/") {
    val login = (parsedBody \ "login").extractOpt[String].getOrElse("")

    userService.checkUserExistenceFor(login, login) match {
      case Right(e) => JsonWrapper("No user with given login/e-mail found.")
      case _ => {
        passwordRecoveryService.sendResetCodeToUser(login)
        JsonWrapper("success")
      }
    }
  }

  post("/:code") {
    val code = params("code")
    val password = (parsedBody \ "password").extractOpt[String].getOrElse("")
    if (!StringUtils.isBlank(password)) {
      passwordRecoveryService.performPasswordReset(code, password) match {
        case Left(e) => halt(403, JsonWrapper(e))
        case _ =>
      }
    } else {
      halt(400, JsonWrapper("missingpassword"))
    }
  }
}
