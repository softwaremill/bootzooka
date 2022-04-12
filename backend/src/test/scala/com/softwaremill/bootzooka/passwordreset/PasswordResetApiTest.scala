package com.softwaremill.bootzooka.passwordreset

import com.softwaremill.bootzooka.email.sender.DummyEmailSender
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.passwordreset.PasswordResetApi.{ForgotPassword_OUT, PasswordReset_OUT}
import com.softwaremill.bootzooka.test._
import org.scalatest.concurrent.Eventually
import sttp.model.StatusCode

class PasswordResetApiTest extends BaseTest with Eventually with TestDependencies with TestSupport {

  "/passwordreset" should "reset the password" in {
    // given
    val RegisteredUser(login, email, password, _) = requests.newRegisteredUsed()
    val newPassword = password + password

    // when
    val response1 = requests.forgotPassword(login)
    response1.body.shouldDeserializeTo[ForgotPassword_OUT]

    // then
    val code = eventually {
      codeSentToEmail(email)
    }

    // when
    val response2 = requests.resetPassword(code, newPassword)
    response2.body.shouldDeserializeTo[PasswordReset_OUT]

    // then
    requests.loginUser(login, password, None).code shouldBe StatusCode.Unauthorized
    requests.loginUser(login, newPassword, None).code shouldBe StatusCode.Ok
  }

  "/passwordreset" should "reset the password once using the given code" in {
    // given
    val RegisteredUser(login, email, password, _) = requests.newRegisteredUsed()
    val newPassword = password + password
    val newerPassword = newPassword + newPassword

    // when
    val response1 = requests.forgotPassword(login)
    response1.body.shouldDeserializeTo[ForgotPassword_OUT]

    // then
    val code = eventually {
      codeSentToEmail(email)
    }

    // when
    requests.resetPassword(code, newPassword).body.shouldDeserializeTo[PasswordReset_OUT]
    requests.resetPassword(code, newPassword).body.shouldDeserializeToError

    // then
    requests.loginUser(login, newPassword, None).code shouldBe StatusCode.Ok
    requests.loginUser(login, newerPassword, None).code shouldBe StatusCode.Unauthorized
  }

  "/passwordreset/forgot" should "end up with Ok HTTP status code and do not send and email if user was not found" in {
    // given
    val RegisteredUser(_, email, _, _) = requests.newRegisteredUsed()

    // when
    val response1 = requests.forgotPassword("wrongUser")

    // then
    response1.code shouldBe StatusCode.Ok
    eventually {
      codeWasNotSentToEmail(email)
    }
  }

  "/passwordreset" should "not reset the password given an invalid code" in {
    // given
    val RegisteredUser(login, _, password, _) = requests.newRegisteredUsed()
    val newPassword = password + password

    // when
    val response2 = requests.resetPassword("invalid", newPassword)
    response2.body.shouldDeserializeToError

    // then
    requests.loginUser(login, password, None).code shouldBe StatusCode.Ok
    requests.loginUser(login, newPassword, None).code shouldBe StatusCode.Unauthorized
  }

  def codeSentToEmail(email: String): String = {
    dependencies.emailService.sendBatch().unwrap

    val emailData = DummyEmailSender
      .findSentEmail(email, "SoftwareMill Bootzooka password reset")
      .getOrElse(throw new IllegalStateException(s"No password reset email sent to $email!"))

    codeFromResetPasswordEmail(emailData.content)
      .getOrElse(throw new IllegalStateException(s"No code found in: $emailData"))
  }

  def codeWasNotSentToEmail(email: String): Unit = {
    dependencies.emailService.sendBatch().unwrap

    val maybeEmail = DummyEmailSender.findSentEmail(email, "SoftwareMill Bootzooka password reset")
    maybeEmail match {
      case Some(emailData) =>
        throw new IllegalStateException(s"There should be no password reset email sent to $email, but instead found $emailData")
      case None => ()
    }
  }

  def codeFromResetPasswordEmail(email: String): Option[String] = {
    val regexp = "code=([\\w]*)".r
    regexp.findFirstMatchIn(email).map(_.group(1))
  }
}
