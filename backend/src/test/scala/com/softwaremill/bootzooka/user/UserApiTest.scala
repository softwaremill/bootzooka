package com.softwaremill.bootzooka.user

import com.softwaremill.bootzooka.email.sender.DummyEmailSender
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.test.{BaseTest, RegisteredUser, TestDependencies, TestSupport}
import com.softwaremill.bootzooka.user.UserApi._
import org.scalatest.concurrent.Eventually
import sttp.model.StatusCode

import scala.concurrent.duration._

class UserApiTest extends BaseTest with Eventually with TestDependencies with TestSupport {

  "/user/register" should "register" in {
    // given
    val (login, email, password) = requests.randomLoginEmailPassword()

    // when
    val response1 = requests.registerUser(login, email, password)

    // then
    response1.code shouldBe StatusCode.Ok
    val apiKey = response1.body.shouldDeserializeTo[Register_OUT].apiKey

    // when
    val response4 = requests.getUser(apiKey)

    // then
    val body = response4.body.shouldDeserializeTo[GetUser_OUT]
    body.email shouldBe email
  }

  "/user/register" should "register and ignore leading and trailing spaces" in {
    // given
    val (login, email, password) = requests.randomLoginEmailPassword()

    // when
    val response1 = requests.registerUser("   " + login + "   ", "   " + email + "   ", password)

    // then
    response1.code shouldBe StatusCode.Ok
    val apiKey = response1.body.shouldDeserializeTo[Register_OUT].apiKey

    // when
    val response4 = requests.getUser(apiKey)

    // then
    val body = response4.body.shouldDeserializeTo[GetUser_OUT]
    body.email shouldBe email
  }

  "/user/register" should "not register if data is invalid" in {
    // given
    val (_, email, password) = requests.randomLoginEmailPassword()

    // when
    val response1 = requests.registerUser("x", email, password) // too short

    // then
    response1.code shouldBe StatusCode.BadRequest
    response1.body.shouldDeserializeToError
  }

  "/user/register" should "not register if email is taken" in {
    // given
    val (login, email, password) = requests.randomLoginEmailPassword()

    // when
    val response1 = requests.registerUser(login + "1", email, password)
    val response2 = requests.registerUser(login + "2", email, password)

    // then
    response1.code shouldBe StatusCode.Ok
    response2.code shouldBe StatusCode.BadRequest
  }

  "/user/register" should "send a welcome email" in {
    // when
    val RegisteredUser(login, email, _, _) = requests.newRegisteredUsed()

    // then
    dependencies.emailService.sendBatch().unwrap
    DummyEmailSender.findSentEmail(email, s"registration confirmation for user $login").isDefined shouldBe true
  }

  "/user/login" should "login the user using the login" in {
    // given
    val RegisteredUser(login, _, password, _) = requests.newRegisteredUsed()

    // when
    val response1 = requests.loginUser(login, password)

    // then
    response1.body.shouldDeserializeTo[Login_OUT]
  }

  "/user/login" should "login the user using the email" in {
    // given
    val RegisteredUser(_, email, password, _) = requests.newRegisteredUsed()

    // when
    val response1 = requests.loginUser(email, password)

    // then
    response1.body.shouldDeserializeTo[Login_OUT]
  }

  "/user/login" should "login the user using uppercase email" in {
    // given
    val RegisteredUser(_, email, password, _) = requests.newRegisteredUsed()

    // when
    val response1 = requests.loginUser(email.toUpperCase, password)

    // then
    response1.body.shouldDeserializeTo[Login_OUT]
  }

  "/user/login" should "login the user with leading or trailing spaces" in {
    // given
    val RegisteredUser(login, _, password, _) = requests.newRegisteredUsed()

    // when
    val response1 = requests.loginUser("   " + login + "   ", password)

    // then
    response1.body.shouldDeserializeTo[Login_OUT]
  }

  "/user/login" should "login the user for the given number of hours" in {
    // given
    val RegisteredUser(login, _, password, _) = requests.newRegisteredUsed()

    // when
    val apiKey = requests.loginUser(login, password, Some(3)).body.shouldDeserializeTo[Login_OUT].apiKey

    // then
    requests.getUser(apiKey).body.shouldDeserializeTo[GetUser_OUT]

    testClock.forward(2.hours)
    requests.getUser(apiKey).body.shouldDeserializeTo[GetUser_OUT]

    testClock.forward(2.hours)
    requests.getUser(apiKey).code shouldBe StatusCode.Unauthorized
  }

  "/user/login" should "respond with 403 HTTP status code and 'Incorrect login/email or password' message if user was not found" in {
    // given
    val RegisteredUser(_, _, password, _) = requests.newRegisteredUsed()

    // when
    val response = requests.loginUser("unknownLogin", password, Some(3))
    response.code shouldBe StatusCode.Unauthorized
    response.body.shouldDeserializeToError shouldBe "Incorrect login/email or password"
  }

  "/user/login" should "respond with 403 HTTP status code and 'Incorrect login/email or password' message if password is incorrect for user" in {
    // given
    val RegisteredUser(login, _, _, _) = requests.newRegisteredUsed()

    // when
    val response = requests.loginUser(login, "wrongPassword", Some(3))
    response.code shouldBe StatusCode.Unauthorized
    response.body.shouldDeserializeToError shouldBe "Incorrect login/email or password"
  }

  "/user/info" should "respond with 403 if the token is invalid" in {
    requests.getUser("invalid").code shouldBe StatusCode.Unauthorized
  }

  "/user/changepassword" should "change the password" in {
    // given
    val RegisteredUser(login, _, password, apiKey) = requests.newRegisteredUsed()
    val newPassword = password + password

    // when
    val response1 = requests.changePassword(apiKey, password, newPassword)

    // then
    response1.body.shouldDeserializeTo[ChangePassword_OUT]
    requests.loginUser(login, password, None).code shouldBe StatusCode.Unauthorized
    requests.loginUser(login, newPassword, None).code shouldBe StatusCode.Ok
  }

  "/user/changepassword" should "not change the password if the current is invalid" in {
    // given
    val RegisteredUser(login, _, password, apiKey) = requests.newRegisteredUsed()
    val newPassword = password + password

    // when
    val response1 = requests.changePassword(apiKey, "invalid", newPassword)

    // then
    response1.code shouldBe StatusCode.Unauthorized
    response1.body.shouldDeserializeToError shouldBe "Incorrect current password"

    requests.loginUser(login, password, None).code shouldBe StatusCode.Ok
    requests.loginUser(login, newPassword, None).code shouldBe StatusCode.Unauthorized
  }

  "/user/changepassword" should "not change the password if the new password is invalid" in {
    // given
    val RegisteredUser(login, _, password, apiKey) = requests.newRegisteredUsed()
    val newPassword = ""

    // when
    val response1 = requests.changePassword(apiKey, password, newPassword)

    // then
    response1.code shouldBe StatusCode.BadRequest
    response1.body.shouldDeserializeToError shouldBe "Password cannot be empty!"

    requests.loginUser(login, password, None).code shouldBe StatusCode.Ok
    requests.loginUser(login, newPassword, None).code shouldBe StatusCode.Unauthorized
  }

  "/user" should "update the login" in {
    // given
    val RegisteredUser(login, email, _, apiKey) = requests.newRegisteredUsed()
    val newLogin = login + login

    // when
    val response1 = requests.updateUser(apiKey, newLogin, email)

    // then
    response1.body.shouldDeserializeTo[UpdateUser_OUT]
    val body = requests.getUser(apiKey).body.shouldDeserializeTo[GetUser_OUT]
    body.login shouldBe newLogin
    body.email shouldBe email
  }

  "/user" should "update the login if the new login is invalid" in {
    // given
    val RegisteredUser(login, email, _, apiKey) = requests.newRegisteredUsed()
    val newLogin = "a"

    // when
    val response1 = requests.updateUser(apiKey, newLogin, email)

    // then
    response1.code shouldBe StatusCode.BadRequest
    response1.body.shouldDeserializeToError shouldBe "Login is too short!"

    val body = requests.getUser(apiKey).body.shouldDeserializeTo[GetUser_OUT]
    body.login shouldBe login
    body.email shouldBe email
  }

  "/user" should "update the email" in {
    // given
    val RegisteredUser(login, _, _, apiKey) = requests.newRegisteredUsed()
    val (_, newEmail, _) = requests.randomLoginEmailPassword()

    // when
    val response1 = requests.updateUser(apiKey, login, newEmail)

    // then
    response1.body.shouldDeserializeTo[UpdateUser_OUT]
    val body = requests.getUser(apiKey).body.shouldDeserializeTo[GetUser_OUT]
    body.login shouldBe login
    body.email shouldBe newEmail
  }

  "/user" should "not update the email if the new email is invalid" in {
    // given
    val RegisteredUser(login, email, _, apiKey) = requests.newRegisteredUsed()
    val newEmail = "aaa"

    // when
    val response1 = requests.updateUser(apiKey, login, newEmail)

    // then
    response1.code shouldBe StatusCode.BadRequest
    response1.body.shouldDeserializeToError shouldBe "Invalid e-mail format!"

    val body = requests.getUser(apiKey).body.shouldDeserializeTo[GetUser_OUT]
    body.login shouldBe login
    body.email shouldBe email
  }

  "/user" should "update the login and email with leading or trailing spaces" in {
    // given
    val RegisteredUser(login, _, _, apiKey) = requests.newRegisteredUsed()
    val newLogin = login + login
    val (_, newEmail, _) = requests.randomLoginEmailPassword()

    // when
    val response1 = requests.updateUser(apiKey, "   " + newLogin + "   ", "   " + newEmail + "   ")

    // then
    response1.body.shouldDeserializeTo[UpdateUser_OUT]
    val body = requests.getUser(apiKey).body.shouldDeserializeTo[GetUser_OUT]
    body.login shouldBe newLogin
    body.email shouldBe newEmail

  }
}
