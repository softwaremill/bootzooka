package com.softwaremill.bootzooka.passwordreset

import com.softwaremill.bootzooka.email.sender.DummyEmailSender
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.passwordreset.PasswordResetApi.{ForgotPassword_IN, ForgotPassword_OUT, PasswordReset_IN, PasswordReset_OUT}
import com.softwaremill.bootzooka.test.{BaseTest, Requests, TestConfig, TestEmbeddedPostgres}
import com.softwaremill.bootzooka.MainModule
import com.softwaremill.bootzooka.config.Config
import monix.eval.Task
import org.http4s._
import org.http4s.syntax.all._
import org.scalatest.concurrent.Eventually
import sttp.client.impl.monix.TaskMonadAsyncError
import sttp.client.testing.SttpBackendStub
import sttp.client.{NothingT, SttpBackend}

class PasswordResetApiTest extends BaseTest with TestEmbeddedPostgres with Eventually {
  lazy val modules: MainModule = new MainModule {
    override def xa: Transactor[Task] = currentDb.xa
    override lazy val baseSttpBackend: SttpBackend[Task, Nothing, NothingT] = SttpBackendStub(TaskMonadAsyncError)
    override lazy val config: Config = TestConfig
  }

  val requests = new Requests(modules)
  import requests._

  "/passwordreset" should "reset the password" in {
    // given
    val RegisteredUser(login, email, password, _) = newRegisteredUsed()
    val newPassword = password + password

    // when
    val response1 = forgotPassword(login)
    response1.shouldDeserializeTo[ForgotPassword_OUT]

    // then
    val code = eventually { codeSentToEmail(email) }

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
    val code = eventually { codeSentToEmail(email) }

    // when
    resetPassword(code, newPassword).shouldDeserializeTo[PasswordReset_OUT]
    resetPassword(code, newPassword).shouldDeserializeToError

    // then
    loginUser(login, newPassword, None).status shouldBe Status.Ok
    loginUser(login, newerPassword, None).status shouldBe Status.Unauthorized
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

  def forgotPassword(loginOrEmail: String): Response[Task] = {
    val request = Request[Task](method = POST, uri = uri"/passwordreset/forgot")
      .withEntity(ForgotPassword_IN(loginOrEmail))

    modules.httpApi.mainRoutes(request).unwrap
  }

  def resetPassword(code: String, password: String): Response[Task] = {
    val request = Request[Task](method = POST, uri = uri"/passwordreset/reset")
      .withEntity(PasswordReset_IN(code, password))

    modules.httpApi.mainRoutes(request).unwrap
  }

  def codeSentToEmail(email: String): String = {
    modules.emailService.sendBatch().unwrap

    val emailData = DummyEmailSender
      .findSentEmail(email, "SoftwareMill Bootzooka password reset")
      .getOrElse(throw new IllegalStateException(s"No password reset email sent to $email!"))

    codeFromResetPasswordEmail(emailData.content)
      .getOrElse(throw new IllegalStateException(s"No code found in: $emailData"))
  }

  def codeFromResetPasswordEmail(email: String): Option[String] = {
    val regexp = "code=([\\w]*)".r
    regexp.findFirstMatchIn(email).map(_.group(1))
  }
}
