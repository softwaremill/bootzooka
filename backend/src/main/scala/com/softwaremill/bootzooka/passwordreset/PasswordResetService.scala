package com.softwaremill.bootzooka.passwordreset

import com.augustnagro.magnum.DbTx
import com.softwaremill.bootzooka.Fail
import com.softwaremill.bootzooka.email.{EmailData, EmailScheduler, EmailSubjectContent, EmailTemplates}
import com.softwaremill.bootzooka.infrastructure.DB
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.security.Auth
import com.softwaremill.bootzooka.user.{User, UserModel}
import com.softwaremill.bootzooka.util.*
import com.softwaremill.bootzooka.util.Strings.{Id, asId, toLowerCased}
import ox.either
import ox.either.*

class PasswordResetService(
    userModel: UserModel,
    passwordResetCodeModel: PasswordResetCodeModel,
    emailScheduler: EmailScheduler,
    emailTemplates: EmailTemplates,
    auth: Auth[PasswordResetCode],
    idGenerator: IdGenerator,
    config: PasswordResetConfig,
    clock: Clock,
    db: DB
) extends Logging:
  def forgotPassword(loginOrEmail: String)(using DbTx): Unit =
    userModel.findByLoginOrEmail(loginOrEmail.toLowerCased) match
      case None       => logger.debug(s"Could not find user with $loginOrEmail login/email")
      case Some(user) =>
        val pcr = createCode(user)
        sendCode(user, pcr)

  private def createCode(user: User)(using DbTx): PasswordResetCode =
    logger.debug(s"Creating password reset code for user: ${user.id}")
    val id = idGenerator.nextId[PasswordResetCode]()
    val validUntil = clock.now().plusMillis(config.codeValid.toMillis)
    val passwordResetCode = PasswordResetCode(id, user.id, validUntil)
    passwordResetCodeModel.insert(passwordResetCode)
    passwordResetCode

  private def sendCode(user: User, code: PasswordResetCode)(using DbTx): Unit =
    logger.debug(s"Scheduling e-mail with reset code for user: ${user.id}")
    emailScheduler.schedule(EmailData(user.emailLowerCase, prepareResetEmail(user, code)))

  private def prepareResetEmail(user: User, code: PasswordResetCode): EmailSubjectContent =
    val resetLink = String.format(config.resetLinkPattern, code.id)
    emailTemplates.passwordReset(user.login, resetLink)

  def resetPassword(code: String, newPassword: String): Either[Fail, Unit] = either {
    val userId = auth(code.asId[PasswordResetCode]).ok()
    logger.debug(s"Resetting password for user: $userId")
    db.transact(userModel.updatePassword(userId, User.hashPassword(newPassword)))
  }
end PasswordResetService
