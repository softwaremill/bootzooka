package com.softwaremill.bootzooka.test

import cats.effect.IO
import com.softwaremill.bootzooka.http.HttpApi
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.user.UserApi._
import org.http4s._
import org.http4s.syntax.all._

import scala.util.Random

class Requests(httpApi: => HttpApi) extends HttpTestSupport {
  case class RegisteredUser(login: String, email: String, password: String, apiKey: String)

  private val random = new Random()

  def randomLoginEmailPassword(): (String, String, String) =
    (random.nextString(12), s"user${random.nextInt(9000)}@bootzooka.com", random.nextString(12))

  def registerUser(login: String, email: String, password: String): Response[IO] = {
    val request = Request[IO](method = POST, uri = uri"/api/v1/user/register")
      .withEntity(Register_IN(login, email, password))

    httpApi.routes(request).unwrap
  }

  def newRegisteredUsed(): RegisteredUser = {
    val (login, email, password) = randomLoginEmailPassword()
    val apiKey = registerUser(login, email, password).shouldDeserializeTo[Register_OUT].apiKey
    RegisteredUser(login, email, password, apiKey)
  }

  def loginUser(loginOrEmail: String, password: String, apiKeyValidHours: Option[Int] = None): Response[IO] = {
    val request = Request[IO](method = POST, uri = uri"/api/v1/user/login")
      .withEntity(Login_IN(loginOrEmail, password, apiKeyValidHours))

    httpApi.routes(request).unwrap
  }

  def getUser(apiKey: String): Response[IO] = {
    val request = Request[IO](method = GET, uri = uri"/api/v1/user")
    httpApi.routes(authorizedRequest(apiKey, request)).unwrap
  }

  def changePassword(apiKey: String, password: String, newPassword: String): Response[IO] = {
    val request = Request[IO](method = POST, uri = uri"/api/v1/user/changepassword")
      .withEntity(ChangePassword_IN(password, newPassword))

    httpApi.routes(authorizedRequest(apiKey, request)).unwrap
  }

  def updateUser(apiKey: String, login: String, email: String): Response[IO] = {
    val request = Request[IO](method = POST, uri = uri"/api/v1/user")
      .withEntity(UpdateUser_IN(login, email))

    httpApi.routes(authorizedRequest(apiKey, request)).unwrap
  }

}
