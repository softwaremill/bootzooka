package com.softwaremill.bootzooka.passwordreset

import cats.implicits._
import com.softwaremill.bootzooka.email.{EmailData, EmailScheduler, EmailSubjectContent, EmailTemplates}
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.security.Auth
import com.softwaremill.bootzooka.user.{User, UserModel}
import com.softwaremill.bootzooka.util._
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

class PasswordResetService(
    userModel: UserModel,
    passwordResetCodeModel: PasswordResetCodeModel,
    emailScheduler: EmailScheduler,
    emailTemplates: EmailTemplates,
    auth: Auth[PasswordResetCode],
    idGenerator: IdGenerator,
    config: PasswordResetConfig,
    clock: Clock,
    xa: Transactor[Task]
) extends StrictLogging {

  def forgotPassword(loginOrEmail: String): ConnectionIO[Unit] = {
    userModel.findByLoginOrEmail(loginOrEmail.lowerCased).flatMap {
      case None =>
        logger.debug(s"Could not find user with $loginOrEmail login/email")
        ().pure[ConnectionIO]
      case Some(user) =>
        createCode(user).flatMap(sendCode(user, _))
    }
  }

  private def createCode(user: User): ConnectionIO[PasswordResetCode] = {
    logger.debug(s"Creating password reset code for user: ${user.id}")

    for {
      id <- idGenerator.nextId[PasswordResetCode]().to[ConnectionIO]
      validUntil <- clock
        .now()
        .map { value =>
          value.plusMillis(config.codeValid.toMillis)
        }
        .to[ConnectionIO]
      passwordResetCode = PasswordResetCode(id, user.id, validUntil)
      connection <- passwordResetCodeModel.insert(passwordResetCode).map(_ => passwordResetCode)
    } yield connection
  }

  private def sendCode(user: User, code: PasswordResetCode): ConnectionIO[Unit] = {
    logger.debug(s"Scheduling e-mail with reset code for user: ${user.id}")
    emailScheduler(EmailData(user.emailLowerCased, prepareResetEmail(user, code)))
  }

  private def prepareResetEmail(user: User, code: PasswordResetCode): EmailSubjectContent = {
    val resetLink = String.format(config.resetLinkPattern, code.id)
    emailTemplates.passwordReset(user.login, resetLink)
  }

  def resetPassword(code: String, newPassword: String): Task[Unit] = {
    for {
      userId <- auth(code.asInstanceOf[Id])
      _ = logger.debug(s"Resetting password for user: $userId")
      _ <- userModel.updatePassword(userId, User.hashPassword(newPassword)).transact(xa)
    } yield ()
  }
}
