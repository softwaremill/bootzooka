package pl.softwaremill.bootstrap.service

import schedulers.EmailSendingService
import org.slf4j.LoggerFactory
import pl.softwaremill.bootstrap.dao.{PasswordResetCodeDAO, UserDAO}
import templates.EmailContentWithSubject
import util.Random
import pl.softwaremill.bootstrap.domain.{User, PasswordResetCode}
import org.joda.time.DateTime
import com.weiglewilczek.slf4s.Logging

/**
 * .
 */
class PasswordRecoveryService(userDao: UserDAO, codeDao: PasswordResetCodeDAO, emailSendingService: EmailSendingService) extends Logging {
  def sendResetCodeToUser(login: String) {
    logger.debug("Preparing to generate and send reset code to user")
    logger.debug("Searching for user")
    val userOption = userDao.findByLoginOrEmail(login)

    if (userOption.isDefined) {
      logger.debug("User found")
      val user = userOption.get
      val code = PasswordResetCode(code = generateCode(), userId = user._id)
      storeCode(code)
      sendCode(user.email, code)
    }
  }

  final val chars = ('a' to 'z') ++ (0 to 9)

  private def generateCode(length: Int = 32) = {
    def appendRandomChar(input: String, counter: Int): String = {
      if (counter == 0) input
      else appendRandomChar(input + chars(Random.nextInt(chars.length)), counter - 1)
    }

    logger.debug("Generating reset code")
    appendRandomChar("", length)
  }

  private def storeCode(code: PasswordResetCode) {
    logger.debug("Storing code")
    codeDao.store(code)
  }

  private def sendCode(address: String, code: PasswordResetCode) {
    emailSendingService.scheduleEmail(address, prepareResetEmail(code))
    logger.debug("E-mail with reset link scheduled")
  }

  private def prepareResetEmail(code: PasswordResetCode) = {
    logger.debug("Preparing e-mail with reset link")
    new EmailContentWithSubject("http://localhost:8080/password-recovery?code=" + code.code, "SML Bootstrap password recovery")
  }
}