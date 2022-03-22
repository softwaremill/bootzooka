package com.softwaremill.bootzooka.passwordreset

import cats.effect.IO
import com.softwaremill.bootzooka.email.sender.DummyEmailSender
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.passwordreset.PasswordResetApi.{
  ForgotPassword_IN,
  ForgotPassword_OUT,
  PasswordReset_IN,
  PasswordReset_OUT
}
import com.softwaremill.bootzooka.test.{TestDependencies, BaseTest, Requests}
import org.http4s._
import org.http4s.syntax.all._
import org.scalatest.concurrent.Eventually

class PasswordResetApiTest extends BaseTest with Eventually with TestDependencies {
  lazy val requests = new Requests(dependencies.api)
  import requests._

  "/passwordreset" should "reset the password" in {
    // given
    val RegisteredUser(login, email, password, _) = newRegisteredUsed()
    val newPassword = password + password

    // when
    val response1 = forgotPassword(login)
    response1.shouldDeserializeTo[ForgotPassword_OUT]

    // then
    val code = eventually {
      codeSentToEmail(email)
    }

    // when
    val response2 = resetPassword(code, newPassword)
    response2.shouldDeserializeTo[PasswordReset_OUT]

    // then
    loginUser(login, password, None).status shouldBe Status.Unauthorized
    loginUser(login, newPassword, None).status shouldBe Status.Ok
  }

  "/passwordreset" should "reset the password once using the given code" in {
    // given
    val RegisteredUser(login, email, password, _) = newRegisteredUsed()
    val newPassword = password + password
    val newerPassword = newPassword + newPassword

    // when
    val response1 = forgotPassword(login)
    response1.shouldDeserializeTo[ForgotPassword_OUT]

    // then
    val code = eventually {
      codeSentToEmail(email)
    }

    // when
    resetPassword(code, newPassword).shouldDeserializeTo[PasswordReset_OUT]
    resetPassword(code, newPassword).shouldDeserializeToError

    // then
    loginUser(login, newPassword, None).status shouldBe Status.Ok
    loginUser(login, newerPassword, None).status shouldBe Status.Unauthorized
  }

  "/passwordreset/forgot" should "end up with Ok HTTP status code and do not send and email if user was not found" in {
    // given
    val RegisteredUser(_, email, _, _) = newRegisteredUsed()

    // when
    val response1 = forgotPassword("wrongUser")

    // then
    response1.status shouldBe Status.Ok
    eventually {
      codeWasNotSentToEmail(email)
    }
  }

  "/passwordreset" should "not reset the password given an invalid code" in {
    // given
    val RegisteredUser(login, _, password, _) = newRegisteredUsed()
    val newPassword = password + password

    // when
    val response2 = resetPassword("invalid", newPassword)
    response2.shouldDeserializeToError

    // then
    loginUser(login, password, None).status shouldBe Status.Ok
    loginUser(login, newPassword, None).status shouldBe Status.Unauthorized
  }

  def forgotPassword(loginOrEmail: String): Response[IO] = {
    val request = Request[IO](method = POST, uri = uri"/api/v1/passwordreset/forgot")
      .withEntity(ForgotPassword_IN(loginOrEmail))

    dependencies.api.routes(request).unwrap
  }

  def resetPassword(code: String, password: String): Response[IO] = {
    val request = Request[IO](method = POST, uri = uri"/api/v1/passwordreset/reset")
      .withEntity(PasswordReset_IN(code, password))

    dependencies.api.routes(request).unwrap
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
