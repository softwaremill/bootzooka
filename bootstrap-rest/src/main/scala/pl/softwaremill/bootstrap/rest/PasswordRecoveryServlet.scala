package pl.softwaremill.bootstrap.rest

/**
 * Servlet handling requests related to password recovery.
 */
class PasswordRecoveryServlet extends JsonServlet {
  post("/") {
    logger.debug("generate reset code and send it to e-mail related to the user")
  }
}
