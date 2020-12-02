package com.softwaremill.bootzooka.passwordreset

import java.time.Clock
import java.time.temporal.ChronoUnit

import cats.implicits._
import com.softwaremill.bootzooka.email.{EmailData, EmailScheduler, EmailSubjectContent, EmailTemplates}
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.security.Auth
import com.softwaremill.bootzooka.user.{User, UserModel}
import com.softwaremill.bootzooka.util._
import com.typesafe.scalalogging.StrictLogging
import doobie.implicits.AsyncConnectionIO
import monix.eval.Task


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

  def forgotPassword(loginOrEmail: String): Task[ConnectionIO[Unit]] = {
    Task { userModel.findByLoginOrEmail(loginOrEmail.lowerCased) }.flatMap { conn =>
      val connectionIO: ConnectionIO[Unit] = conn.map {
        case None => Task { ().pure[ConnectionIO] }
        case Some(user) => createCode(user).map(sendCode(user, _) )
      }
      Task { connectionIO }
    }
  }

  private def createCode(user: User): Task[ConnectionIO[PasswordResetCode]] = {
    logger.debug(s"Creating password reset code for user: ${user.id}")
    val validUntil = clock.instant().plus(config.codeValid.toMinutes, ChronoUnit.MINUTES)

    for {
      id <- idGenerator.nextId[PasswordResetCode]()
      code = PasswordResetCode(id, user.id, validUntil)
    } yield passwordResetCodeModel.insert(code).map(_ => code)
  }

  private def sendCode(user: User, connectionIO: ConnectionIO[PasswordResetCode]): ConnectionIO[Unit] = {
    logger.debug(s"Scheduling e-mail with reset code for user: ${user.id}")
    for {
      code <- connectionIO
      mail = prepareResetEmail(user, code)
    } yield emailScheduler(EmailData(user.emailLowerCased, mail))
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
