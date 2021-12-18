package com.softwaremill.bootzooka.passwordreset

import cats.effect.IO
import cats.implicits._
import com.softwaremill.bootzooka.email.{EmailData, EmailScheduler, EmailSubjectContent, EmailTemplates}
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.security.Auth
import com.softwaremill.bootzooka.user.{User, UserModel}
import com.softwaremill.bootzooka.util._
import com.typesafe.scalalogging.StrictLogging

class PasswordResetService(
    userModel: UserModel,
    passwordResetCodeModel: PasswordResetCodeModel,
    emailScheduler: EmailScheduler,
    emailTemplates: EmailTemplates,
    auth: Auth[PasswordResetCode],
    idGenerator: IdGenerator,
    config: PasswordResetConfig,
    clock: Clock,
    xa: Transactor[IO]
) extends StrictLogging {

  def forgotPassword(loginOrEmail: String): IO[ConnectionIO[Unit]] = {
    userModel.findByLoginOrEmail(loginOrEmail.lowerCased)
      .map {
        case None =>
          logger.debug(s"Could not find user with $loginOrEmail login/email")
          IO(().pure[ConnectionIO])
        case Some(user) =>
          createCode(user)
            .flatMap(_.transact(xa).flatMap(pcr => sendCode(user, pcr)))
      }
      .transact(xa)
      .flatten

  }

  private def createCode(user: User): IO[ConnectionIO[PasswordResetCode]] = {
    logger.debug(s"Creating password reset code for user: ${user.id}")
    for {
      id <- idGenerator.nextId[PasswordResetCode]()
      validUntil <- clock
        .now()
        .map { value =>
          value.plusMillis(config.codeValid.toMillis)
        }
    } yield {
      val passwordResetCode: PasswordResetCode = PasswordResetCode(id, user.id, validUntil)
      passwordResetCodeModel.insert(passwordResetCode).map(_ => passwordResetCode)
    }
  }

  private def sendCode(user: User, code: PasswordResetCode): IO[ConnectionIO[Unit]] = {
    logger.debug(s"Scheduling e-mail with reset code for user: ${user.id}")
    emailScheduler(EmailData(user.emailLowerCased, prepareResetEmail(user, code)))
  }

  private def prepareResetEmail(user: User, code: PasswordResetCode): EmailSubjectContent = {
    val resetLink = String.format(config.resetLinkPattern, code.id)
    emailTemplates.passwordReset(user.login, resetLink)
  }

  def resetPassword(code: String, newPassword: String): IO[Unit] = {
    for {
      userId <- auth(code.asInstanceOf[Id])
      _ = logger.debug(s"Resetting password for user: $userId")
      _ <- userModel.updatePassword(userId, User.hashPassword(newPassword)).transact(xa)
    } yield ()
  }
}
