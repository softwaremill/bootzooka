package com.softwaremill.bootzooka.test

import cats.effect.IO
import com.softwaremill.bootzooka.passwordreset.PasswordResetApi.{ForgotPassword_IN, PasswordReset_IN}
import com.softwaremill.bootzooka.user.UserApi._
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import sttp.client3
import sttp.client3.{Response, SttpBackend, UriContext, basicRequest}

import scala.util.Random

class Requests(backend: SttpBackend[IO, Any]) extends TestSupport {

  private val random = new Random()

  def randomLoginEmailPassword(): (String, String, String) =
    (random.nextString(12), s"user${random.nextInt(9000)}@bootzooka.com", random.nextString(12))

  private val basePath = "http://localhost:8080/api/v1"

  def registerUser(login: String, email: String, password: String): Response[Either[String, String]] = {
    basicRequest
      .post(uri"$basePath/user/register")
      .body(Register_IN(login, email, password).asJson.noSpaces)
      .send(backend)
      .unwrap
  }

  def newRegisteredUsed(): RegisteredUser = {
    val (login, email, password) = randomLoginEmailPassword()
    val apiKey = registerUser(login, email, password).body.shouldDeserializeTo[Register_OUT].apiKey
    RegisteredUser(login, email, password, apiKey)
  }

  def loginUser(loginOrEmail: String, password: String, apiKeyValidHours: Option[Int] = None): Response[Either[String, String]] = {
    basicRequest
      .post(uri"$basePath/user/login")
      .body(Login_IN(loginOrEmail, password, apiKeyValidHours).asJson.noSpaces)
      .send(backend)
      .unwrap
  }

  def getUser(apiKey: String): client3.Response[Either[String, String]] = {
    basicRequest
      .get(uri"$basePath/user")
      .header("Authorization", s"Bearer $apiKey")
      .send(backend)
      .unwrap
  }

  def changePassword(apiKey: String, password: String, newPassword: String): Response[Either[String, String]] = {
    basicRequest
      .post(uri"$basePath/user/changepassword")
      .body(ChangePassword_IN(password, newPassword).asJson.noSpaces)
      .header("Authorization", s"Bearer $apiKey")
      .send(backend)
      .unwrap
  }

  def updateUser(apiKey: String, login: String, email: String): Response[Either[String, String]] = {
    basicRequest
      .post(uri"$basePath/user")
      .body(UpdateUser_IN(login, email).asJson.noSpaces)
      .header("Authorization", s"Bearer $apiKey")
      .send(backend)
      .unwrap
  }

  def forgotPassword(loginOrEmail: String): Response[Either[String, String]] = {
    basicRequest
      .post(uri"$basePath/passwordreset/forgot")
      .body(ForgotPassword_IN(loginOrEmail).asJson.noSpaces)
      .send(backend)
      .unwrap
  }

  def resetPassword(code: String, password: String): Response[Either[String, String]] = {
    basicRequest
      .post(uri"$basePath/passwordreset/reset")
      .body(PasswordReset_IN(code, password).asJson.noSpaces)
      .send(backend)
      .unwrap
  }
}
