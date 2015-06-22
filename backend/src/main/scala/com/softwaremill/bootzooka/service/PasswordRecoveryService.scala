package com.softwaremill.bootzooka.service

import com.softwaremill.bootzooka.common.Utils
import com.softwaremill.bootzooka.dao.{PasswordResetCodeDao, UserDao}
import com.softwaremill.bootzooka.domain.{PasswordResetCode, User}
import com.softwaremill.bootzooka.service.config.CoreConfig
import com.softwaremill.bootzooka.service.email.EmailService
import com.softwaremill.bootzooka.service.templates.EmailTemplatingEngine
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

class PasswordRecoveryService(
    userDao: UserDao,
    codeDao: PasswordResetCodeDao,
    emailService: EmailService,
    emailTemplatingEngine: EmailTemplatingEngine,
    config: CoreConfig
)(implicit ec: ExecutionContext) extends LazyLogging {

  def sendResetCodeToUser(login: String): Future[Unit] = {
    logger.debug("Preparing to generate and send reset code to user")
    logger.debug("Searching for user")
    val userFut = userDao.findByLoginOrEmail(login)
    userFut.flatMap {
      case Some(user) =>
        logger.debug("User found")
        val code = randomPass(user)
        storeCode(code).flatMap(_ => sendCode(code))
      case None =>
        logger.debug(s"User not found: $login")
        Future.successful((): Unit)
    }
  }

  private def randomPass(user: User): PasswordResetCode = PasswordResetCode(Utils.randomString(32), user)

  private def storeCode(code: PasswordResetCode): Future[Unit] = {
    logger.debug("Storing code")
    codeDao.add(code)
  }

  private def sendCode(code: PasswordResetCode): Future[Unit] = {
    logger.debug("Scheduling e-mail with reset code")
    emailService.scheduleEmail(code.user.email, prepareResetEmail(code.user, code))
  }

  private def prepareResetEmail(user: User, code: PasswordResetCode) = {
    logger.debug("Preparing content for password reset e-mail")
    val resetLink = String.format(config.resetLinkPattern, code.code)
    emailTemplatingEngine.passwordReset(user.login, resetLink)
  }

  def performPasswordReset(code: String, newPassword: String): Future[Either[String, Boolean]] = {
    logger.debug("Performing password reset")
    codeDao.load(code).flatMap {
      case Some(c) =>
        if (c.validTo.isAfter(new DateTime())) {
          for {
            _ <- changePassword(c, newPassword)
            _ <- invalidateResetCode(c)
          } yield Right(true)
        }
        else {
          invalidateResetCode(c).map(_ => Left("Your reset code is invalid. Please try again."))
        }
      case None =>
        logger.debug("Reset code not found")
        Future.successful(Left("Your reset code is invalid. Please try again."))
    }
  }

  private def changePassword(code: PasswordResetCode, newPassword: String) = {
    userDao.changePassword(code.user.id, User.encryptPassword(newPassword, code.user.salt))
  }

  private def invalidateResetCode(code: PasswordResetCode) = {
    codeDao.remove(code)
  }
}