package pl.softwaremill.bootstrap.service

import schedulers.EmailSendingService
import org.slf4j.LoggerFactory
import pl.softwaremill.bootstrap.dao.UserDAO
import templates.EmailContentWithSubject
import util.Random

/**
 * .
 */
class PasswordRecoveryService(userDao: UserDAO, emailSendingService: EmailSendingService) {
  private final val logger = LoggerFactory.getLogger(getClass.getName)

  def sendResetCodeToUser(login: String) {
    val user = userDao.findByLoginOrEmail(login)

    if (user.isDefined) {
      val code = generateCode()
      storeCode(code)
      sendCode(user.get.email, code)
    }
  }

  final val chars = ('a' to 'z') ++ (0 to 9)

  private def generateCode(length: Int = 32) = {
    def appendRandomChar(input: String, counter: Int): String = {
      if (counter == 0) input
      else appendRandomChar(input + chars(Random.nextInt(chars.length)), counter - 1)
    }

    appendRandomChar("", length)
  }

  private def storeCode(code: String) {

  }

  private def sendCode(address: String, code: String) {
    emailSendingService.scheduleEmail(address, prepareResetEmail(code))
  }

  private def prepareResetEmail(code: String) = {
    new EmailContentWithSubject("http://localhost:8080/password-recovery?code=" + code, "SML Bootstrap password recovery")
  }
}