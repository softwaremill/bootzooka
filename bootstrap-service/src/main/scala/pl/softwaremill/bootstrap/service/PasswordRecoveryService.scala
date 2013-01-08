package pl.softwaremill.bootstrap.service

import schedulers.EmailSendingService
import org.slf4j.LoggerFactory
import pl.softwaremill.bootstrap.dao.UserDAO

/**
 * .
 */
class PasswordRecoveryService(userDao: UserDAO, emailSendingService: EmailSendingService) {
  private final val logger = LoggerFactory.getLogger(getClass.getName)

  def sendResetCodeToUser(login: String) {
    val user = userDao.findByLoginOrEmail(login)

    if (user.isDefined) {
      val code = generateCodeFor(login)
      sendCodeToUser(user.get.email, code)
    }
  }

  private def generateCodeFor(login: String): String = {
    ""
  }

  private def sendCodeToUser(address:String, code: String) {
    emailSendingService.scheduleEmail(address, prepareResetEmail(code))
  }

  private def prepareResetEmail(code:String) = {
    null
  }
}
