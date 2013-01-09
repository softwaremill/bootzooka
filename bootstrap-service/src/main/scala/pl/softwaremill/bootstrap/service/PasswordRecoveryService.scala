package pl.softwaremill.bootstrap.service

import schedulers.EmailSendingService
import org.slf4j.LoggerFactory
import pl.softwaremill.bootstrap.dao.{PasswordResetCodeDAO, UserDAO}
import templates.{EmailTemplatingEngine, EmailContentWithSubject}
import util.Random
import pl.softwaremill.bootstrap.domain.{User, PasswordResetCode}
import org.joda.time.DateTime
import com.weiglewilczek.slf4s.Logging
import pl.softwaremill.common.util.RichString

/**
 * .
 */
class PasswordRecoveryService(userDao: UserDAO, codeDao: PasswordResetCodeDAO,
                              emailSendingService: EmailSendingService, emailTemplatingEngine:EmailTemplatingEngine) extends Logging {
  def sendResetCodeToUser(login: String) {
    logger.debug("Preparing to generate and send reset code to user")
    logger.debug("Searching for user")
    val userOption = userDao.findByLoginOrEmail(login)

    userOption match {
      case Some(user) => {
        logger.debug("User found")
        val user = userOption.get
        val code = PasswordResetCode(code = RichString.generateRandom(32), userId = user._id)
        storeCode(code)
        sendCode(user, code)
      }
      case None => logger.debug("User not found")
    }
  }

  private def storeCode(code: PasswordResetCode) {
    logger.debug("Storing code")
    codeDao.store(code)
  }

  private def sendCode(user: User, code: PasswordResetCode) {
    emailSendingService.scheduleEmail(user.email, prepareResetEmail(user, code))
    logger.debug("E-mail with reset link scheduled")
  }

  private def prepareResetEmail(user: User, code: PasswordResetCode) = {
    logger.debug("Preparing e-mail with reset link")
    val resetLink: String = "http://localhost:8080/password-recovery?code=" + code.code
    emailTemplatingEngine.passwordReset(user.login, resetLink)
  }
}