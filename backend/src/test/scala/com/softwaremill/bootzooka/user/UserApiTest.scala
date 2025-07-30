package com.softwaremill.bootzooka.user

import com.softwaremill.bootzooka.Fail
import com.softwaremill.bootzooka.email.sender.DummyEmailSender
import com.softwaremill.bootzooka.test.{BaseTest, RegisteredUser, TestDependencies}
import com.softwaremill.bootzooka.user.UserApi.*
import org.scalatest.EitherValues
import org.scalatest.concurrent.Eventually
import sttp.model.StatusCode

import scala.concurrent.duration.*

class UserApiTest extends BaseTest with Eventually with TestDependencies with EitherValues:

  "/user/register" should "register" in {
    // given
    val (login, email, password) = requests.randomLoginEmailPassword()

    // when
    val response1 = requests.registerUser(login, email, password)

    // then
    val apiKey = response1.body.value.apiKey

    // when
    val response4 = requests.getUser(apiKey)

    // then
    response4.body.value.email shouldBe email
  }

  "/user/register" should "register and ignore leading and trailing spaces" in {
    // given
    val (login, email, password) = requests.randomLoginEmailPassword()

    // when
    val response1 = requests.registerUser("   " + login + "   ", "   " + email + "   ", password)

    // then
    val apiKey = response1.body.value.apiKey

    // when
    val response4 = requests.getUser(apiKey)

    // then
    response4.body.value.email shouldBe email
  }

  "/user/register" should "not register if data is invalid" in {
    // given
    val (_, email, password) = requests.randomLoginEmailPassword()

    // when
    val response1 = requests.registerUser("x", email, password) // too short

    // then
    response1.code shouldBe StatusCode.BadRequest
    response1.body should matchPattern { case Left(_: Fail) => }
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
    dependencies.emailService.sendBatch()
    DummyEmailSender.findSentEmail(email, s"registration confirmation for user $login").isDefined shouldBe true
  }

  "/user/login" should "login the user using the login" in {
    // given
    val RegisteredUser(login, _, password, _) = requests.newRegisteredUsed()

    // when
    val response1 = requests.loginUser(login, password)

    // then
    response1.body should matchPattern { case Right(_) => }
  }

  "/user/login" should "login the user using the email" in {
    // given
    val RegisteredUser(_, email, password, _) = requests.newRegisteredUsed()

    // when
    val response1 = requests.loginUser(email, password)

    // then
    response1.body should matchPattern { case Right(_) => }
  }

  "/user/login" should "login the user using uppercase email" in {
    // given
    val RegisteredUser(_, email, password, _) = requests.newRegisteredUsed()

    // when
    val response1 = requests.loginUser(email.toUpperCase, password)

    // then
    response1.body should matchPattern { case Right(_) => }
  }

  "/user/login" should "login the user with leading or trailing spaces" in {
    // given
    val RegisteredUser(login, _, password, _) = requests.newRegisteredUsed()

    // when
    val response1 = requests.loginUser("   " + login + "   ", password)

    // then
    response1.body should matchPattern { case Right(_) => }
  }

  "/user/login" should "login the user for the given number of hours" in {
    // given
    val RegisteredUser(login, _, password, _) = requests.newRegisteredUsed()

    // when
    val apiKey = requests.loginUser(login, password, Some(3)).body.value.apiKey

    // then
    requests.getUser(apiKey).body should matchPattern { case Right(_) => }

    testClock.forward(2.hours)
    requests.getUser(apiKey).body should matchPattern { case Right(_) => }

    testClock.forward(2.hours)
    requests.getUser(apiKey).code shouldBe StatusCode.Unauthorized
  }

  "/user/login" should "respond with 403 HTTP status code and 'Incorrect login/email or password' message if user was not found" in {
    // given
    val RegisteredUser(_, _, password, _) = requests.newRegisteredUsed()

    // when
    val response = requests.loginUser("unknownLogin", password, Some(3))
    response.body shouldBe Left(Fail.Unauthorized("Incorrect login/email or password"))
  }

  "/user/login" should "respond with 403 HTTP status code and 'Incorrect login/email or password' message if password is incorrect for user" in {
    // given
    val RegisteredUser(login, _, _, _) = requests.newRegisteredUsed()

    // when
    val response = requests.loginUser(login, "wrongPassword", Some(3))
    response.body shouldBe Left(Fail.Unauthorized("Incorrect login/email or password"))
  }

  "/user/info" should "respond with 403 if the token is invalid" in {
    requests.getUser("invalid").code shouldBe StatusCode.Unauthorized
  }

  "/user/logout" should "logout the user" in {
    // given
    val RegisteredUser(_, _, _, apiKey) = requests.newRegisteredUsed()

    // when
    val response = requests.logoutUser(apiKey)

    // then
    response.code shouldBe StatusCode.Ok

    // when
    val responseAfterLogout = requests.getUser(apiKey)

    // then
    responseAfterLogout.code shouldBe StatusCode.Unauthorized
  }

  "/user/changepassword" should "change the password" in {
    // given
    val RegisteredUser(login, _, password, apiKey) = requests.newRegisteredUsed()
    val newPassword = password + password

    // when
    val response1 = requests.changePassword(apiKey, password, newPassword)

    // then
    response1.body should matchPattern { case Right(_) => }
    requests.loginUser(login, password, None).code shouldBe StatusCode.Unauthorized
    requests.loginUser(login, newPassword, None).code shouldBe StatusCode.Ok
  }

  "/user/changepassword" should "create new session and invalidate all existing user's sessions" in {
    // given
    val RegisteredUser(login, _, password, apiKey1) = requests.newRegisteredUsed()
    val newPassword = password + password

    // login again to create another session
    val apiKey2 = requests.loginUser(login, password, Some(3)).body.value.apiKey

    // when
    val response = requests.changePassword(apiKey1, password, newPassword)

    // then
    val newApiKey = response.body.value.apiKey
    requests.getUser(newApiKey).code shouldBe StatusCode.Ok
    requests.getUser(apiKey1).code shouldBe StatusCode.Unauthorized
    requests.getUser(apiKey2).code shouldBe StatusCode.Unauthorized
  }

  "/user/changepassword" should "not change the password if the current is invalid" in {
    // given
    val RegisteredUser(login, _, password, apiKey) = requests.newRegisteredUsed()
    val newPassword = password + password

    // when
    val response1 = requests.changePassword(apiKey, "invalid", newPassword)

    // then
    response1.body shouldBe Left(Fail.Unauthorized("Incorrect current password"))

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
    response1.body shouldBe Left(Fail.IncorrectInput("Password cannot be empty!"))

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
    response1.body should matchPattern { case Right(_) => }
    val getUserResponseBody = requests.getUser(apiKey).body.value
    getUserResponseBody.login shouldBe newLogin
    getUserResponseBody.email shouldBe email
  }

  "/user" should "update the login if the new login is invalid" in {
    // given
    val RegisteredUser(login, email, _, apiKey) = requests.newRegisteredUsed()
    val newLogin = "a"

    // when
    val response1 = requests.updateUser(apiKey, newLogin, email)

    // then
    response1.body shouldBe Left(Fail.IncorrectInput("Login is too short!"))

    val getUserResponseBody = requests.getUser(apiKey).body.value
    getUserResponseBody.login shouldBe login
    getUserResponseBody.email shouldBe email
  }

  "/user" should "update the email" in {
    // given
    val RegisteredUser(login, _, _, apiKey) = requests.newRegisteredUsed()
    val (_, newEmail, _) = requests.randomLoginEmailPassword()

    // when
    val response1 = requests.updateUser(apiKey, login, newEmail)

    // then
    response1.body should matchPattern { case Right(_) => }
    val getUserResponseBody = requests.getUser(apiKey).body.value
    getUserResponseBody.login shouldBe login
    getUserResponseBody.email shouldBe newEmail
  }

  "/user" should "not update the email if the new email is invalid" in {
    // given
    val RegisteredUser(login, email, _, apiKey) = requests.newRegisteredUsed()
    val newEmail = "aaa"

    // when
    val response1 = requests.updateUser(apiKey, login, newEmail)

    // then
    response1.body shouldBe Left(Fail.IncorrectInput("Invalid e-mail format!"))

    val getUserResponseBody = requests.getUser(apiKey).body.value
    getUserResponseBody.login shouldBe login
    getUserResponseBody.email shouldBe email
  }

  "/user" should "update the login and email with leading or trailing spaces" in {
    // given
    val RegisteredUser(login, _, _, apiKey) = requests.newRegisteredUsed()
    val newLogin = login + login
    val (_, newEmail, _) = requests.randomLoginEmailPassword()

    // when
    val response1 = requests.updateUser(apiKey, "   " + newLogin + "   ", "   " + newEmail + "   ")

    // then
    response1.body should matchPattern { case Right(_) => }
    val getUserResponseBody = requests.getUser(apiKey).body.value
    getUserResponseBody.login shouldBe newLogin
    getUserResponseBody.email shouldBe newEmail
  }
end UserApiTest
