package com.softwaremill.bootzooka.service

import com.softwaremill.bootzooka.common.Utils
import com.softwaremill.bootzooka.dao.passwordResetCode.PasswordResetCodeDao
import com.softwaremill.bootzooka.dao.user.UserDao
import com.softwaremill.bootzooka.domain.{PasswordResetCode, User}
import com.softwaremill.bootzooka.service.config.BootzookaConfig
import com.softwaremill.bootzooka.service.email.EmailScheduler
import com.softwaremill.bootzooka.service.templates.EmailTemplatingEngine
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.joda.time.DateTime

class PasswordRecoveryService(
  userDao: UserDao,
  codeDao: PasswordResetCodeDao,
  emailScheduler: EmailScheduler,
  emailTemplatingEngine: EmailTemplatingEngine,
  config: BootzookaConfig) extends LazyLogging {

  def sendResetCodeToUser(login: String) {
    logger.debug("Preparing to generate and send reset code to user")
    logger.debug("Searching for user")
    val userOption = userDao.findByLoginOrEmail(login)

    userOption match {
      case Some(user) => {
        logger.debug("User found")
        val code = PasswordResetCode(Utils.randomString(32), user.id)
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
    logger.debug("Scheduling e-mail with reset code")
    emailScheduler.scheduleEmail(user.email, prepareResetEmail(user, code))
  }

  private def prepareResetEmail(user: User, code: PasswordResetCode) = {
    logger.debug("Preparing content for password reset e-mail")
    val resetLink = String.format(config.bootzookaResetLinkPattern, code.code)
    emailTemplatingEngine.passwordReset(user.login, resetLink)
  }

  def performPasswordReset(code: String, newPassword: String): Either[String, Boolean] = {
    logger.debug("Performing password reset")
    codeDao.load(code) match {
      case Some(c) => {
        if (c.validTo.isAfter(new DateTime())) {
          changePassword(c, newPassword)
          invalidateResetCode(c)
          Right(true)
        } else {
          invalidateResetCode(c)
          Left("Your reset code is invalid. Please try again.")
        }
      }
      case None => {
        logger.debug("Reset code not found")
        Left("Your reset code is invalid. Please try again.")
      }
    }
  }

  private def changePassword(code: PasswordResetCode, newPassword: String) {
    userDao.load(code.userId) match {
      case Some(u) => userDao.changePassword(u.id, User.encryptPassword(newPassword, u.salt))
      case None => logger.debug("User does not exist")
    }
  }

  private def invalidateResetCode(code: PasswordResetCode) {
    codeDao.delete(code)
  }
}