package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.service.PasswordRecoveryService
import org.apache.commons.lang3.StringUtils
import org.scalatra.BadRequest

/**
 * Servlet handling requests related to password recovery.
 */
class PasswordRecoveryServlet(passwordRecoveryService: PasswordRecoveryService) extends JsonServlet {

  post("/") {
    val login = (parsedBody \ "login").extractOpt[String].getOrElse("")
    passwordRecoveryService.sendResetCodeToUser(login)
  }

  post("/:code") {
    val code = params("code")
    val password = (parsedBody \ "password").extractOpt[String].getOrElse("")
    logger.debug("Code: " + code + ", passwordLength: " + password.length)
    if (!StringUtils.isBlank(password)) {
      passwordRecoveryService.performPasswordReset(code, password)
    } else {
      halt(400)
    }
  }
}
