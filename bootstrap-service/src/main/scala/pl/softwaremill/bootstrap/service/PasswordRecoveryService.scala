package pl.softwaremill.bootstrap.service

import schedulers.EmailSendingService
import org.slf4j.LoggerFactory

/**
 * .
 */
class PasswordRecoveryService(emailSendingService: EmailSendingService) {
  private val logger = LoggerFactory.getLogger(getClass.getName)
  def sendResetCodeToUser(login: String) {
    logger.debug("Generating code and sending email")
  }
}
