package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.service.PasswordRecoveryService
import org.apache.commons.lang3.StringUtils
import org.scalatra.BadRequest
import com.softwaremill.bootzooka.common.JsonWrapper
import com.softwaremill.bootzooka.service.user.UserService

/**
 * Servlet handling requests related to password recovery.
 */
class PasswordRecoveryServlet(passwordRecoveryService: PasswordRecoveryService, userService: UserService) extends JsonServlet {

  post("/") {
    val login = (parsedBody \ "login").extractOpt[String].getOrElse("")
    passwordRecoveryService.sendResetCodeToUser(login)
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
