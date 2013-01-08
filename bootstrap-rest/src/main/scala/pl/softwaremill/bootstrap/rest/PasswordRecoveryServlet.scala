package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.service.PasswordRecoveryService

/**
 * Servlet handling requests related to password recovery.
 */
class PasswordRecoveryServlet(passwordRecoveryService:PasswordRecoveryService) extends JsonServlet {
  post("/") {
    val login = (parsedBody \ "login").extract[String]
    passwordRecoveryService.sendResetCodeToUser(login)
  }
}
