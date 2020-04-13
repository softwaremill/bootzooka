package com.softwaremill.bootzooka.user

import java.time.Clock

import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.email.sender.DummyEmailSender
import com.softwaremill.bootzooka.MainModule
import com.softwaremill.bootzooka.test.{BaseTest, Requests, TestConfig, TestEmbeddedPostgres}
import monix.eval.Task
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.user.UserApi.{ChangePassword_OUT, GetUser_OUT, Login_OUT, Register_OUT, UpdateUser_OUT}
import org.http4s.Status
import org.scalatest.concurrent.Eventually
import sttp.client.impl.monix.TaskMonadAsyncError
import sttp.client.testing.SttpBackendStub
import sttp.client.{NothingT, SttpBackend}

import scala.concurrent.duration._

class UserApiTest extends BaseTest with TestEmbeddedPostgres with Eventually {
  lazy val modules: MainModule = new MainModule {
    override def xa: Transactor[Task] = currentDb.xa
    override lazy val baseSttpBackend: SttpBackend[Task, Nothing, NothingT] = SttpBackendStub(TaskMonadAsyncError)
    override lazy val config: Config = TestConfig
    override lazy val clock: Clock = testClock
  }

  val requests = new Requests(modules)
  import requests._

  "/user/register" should "register" in {
    // given
    val (login, email, password) = randomLoginEmailPassword()

    // when
    val response1 = registerUser(login, email, password)

    // then
    response1.status shouldBe Status.Ok
    val apiKey = response1.shouldDeserializeTo[Register_OUT].apiKey

    // when
    val response4 = getUser(apiKey)

    // then
    response4.shouldDeserializeTo[GetUser_OUT].email shouldBe email
  }

  "/user/register" should "not register if data is invalid" in {
    // given
    val (_, email, password) = randomLoginEmailPassword()

    // when
    val response1 = registerUser("x", email, password) // too short

    // then
    response1.status shouldBe Status.BadRequest
    response1.shouldDeserializeToError
  }

  "/user/register" should "not register if email is taken" in {
    // given
    val (login, email, password) = randomLoginEmailPassword()

    // when
    val response1 = registerUser(login + "1", email, password)
    val response2 = registerUser(login + "2", email, password)

    // then
    response1.status shouldBe Status.Ok
    response2.status shouldBe Status.BadRequest
  }

  "/user/register" should "send a welcome email" in {
    // when
    val RegisteredUser(login, email, _, _) = newRegisteredUsed()

    // then
    modules.emailService.sendBatch().unwrap
    DummyEmailSender.findSentEmail(email, s"registration confirmation for user $login").isDefined shouldBe true
  }

  "/user/login" should "login the user using the login" in {
    // given
    val RegisteredUser(login, _, password, _) = newRegisteredUsed()

    // when
    val response1 = loginUser(login, password)

    // then
    response1.shouldDeserializeTo[Login_OUT]
  }

  "/user/login" should "login the user using the email" in {
    // given
    val RegisteredUser(_, email, password, _) = newRegisteredUsed()

    // when
    val response1 = loginUser(email, password)

    // then
    response1.shouldDeserializeTo[Login_OUT]
  }

  "/user/login" should "login the user using uppercase email" in {
    // given
    val RegisteredUser(_, email, password, _) = newRegisteredUsed()

    // when
    val response1 = loginUser(email.toUpperCase, password)

    // then
    response1.shouldDeserializeTo[Login_OUT]
  }

  "/user/login" should "login the user for the given number of hours" in {
    // given
    val RegisteredUser(login, _, password, _) = newRegisteredUsed()

    // when
    val apiKey = loginUser(login, password, Some(3)).shouldDeserializeTo[Login_OUT].apiKey

    // then
    getUser(apiKey).shouldDeserializeTo[GetUser_OUT]

    testClock.forward(2.hours)
    getUser(apiKey).shouldDeserializeTo[GetUser_OUT]

    testClock.forward(2.hours)
    getUser(apiKey).status shouldBe Status.Unauthorized
  }

  "/user/info" should "respond with 403 if the token is invalid" in {
    getUser("invalid").status shouldBe Status.Unauthorized
  }

  "/user/changepassword" should "change the password" in {
    // given
    val RegisteredUser(login, _, password, apiKey) = newRegisteredUsed()
    val newPassword = password + password

    // when
    val response1 = changePassword(apiKey, password, newPassword)

    // then
    response1.shouldDeserializeTo[ChangePassword_OUT]
    loginUser(login, password, None).status shouldBe Status.Unauthorized
    loginUser(login, newPassword, None).status shouldBe Status.Ok
  }

  "/user/changepassword" should "not change the password if the current is invalid" in {
    // given
    val RegisteredUser(login, _, password, apiKey) = newRegisteredUsed()
    val newPassword = password + password

    // when
    val response1 = changePassword(apiKey, "invalid", newPassword)

    // then
    response1.shouldDeserializeToError
    loginUser(login, password, None).status shouldBe Status.Ok
    loginUser(login, newPassword, None).status shouldBe Status.Unauthorized
  }

  "/user" should "update the login" in {
    // given
    val RegisteredUser(login, email, _, apiKey) = newRegisteredUsed()
    val newLogin = login + login

    // when
    val response1 = updateUser(apiKey, newLogin, email)

    // then
    response1.shouldDeserializeTo[UpdateUser_OUT]
    getUser(apiKey).shouldDeserializeTo[GetUser_OUT].login shouldBe newLogin
    getUser(apiKey).shouldDeserializeTo[GetUser_OUT].email shouldBe email
  }

  "/user" should "update the email" in {
    // given
    val RegisteredUser(login, _, _, apiKey) = newRegisteredUsed()
    val (_, newEmail, _) = randomLoginEmailPassword()

    // when
    val response1 = updateUser(apiKey, login, newEmail)

    // then
    response1.shouldDeserializeTo[UpdateUser_OUT]
    getUser(apiKey).shouldDeserializeTo[GetUser_OUT].login shouldBe login
    getUser(apiKey).shouldDeserializeTo[GetUser_OUT].email shouldBe newEmail
  }
}
