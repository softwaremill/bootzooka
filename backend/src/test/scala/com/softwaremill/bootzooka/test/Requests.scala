package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.MainModule
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.user.UserApi._
import monix.eval.Task
import org.http4s._
import org.http4s.syntax.all._

import scala.util.Random

class Requests(val modules: MainModule) extends HttpTestSupport {

  case class RegisteredUser(login: String, email: String, password: String, apiKey: String)

  private val random = new Random()

  def randomLoginEmailPassword(): (String, String, String) =
    (random.nextString(12), s"user${random.nextInt(9000)}@bootzooka.com", random.nextString(12))

  def registerUser(login: String, email: String, password: String): Response[Task] = {
    val request = Request[Task](method = POST, uri = uri"/user/register")
      .withEntity(Register_IN(login, email, password))

    modules.httpApi.mainRoutes(request).unwrap
  }

  def newRegisteredUsed(): RegisteredUser = {
    val (login, email, password) = randomLoginEmailPassword()
    val apiKey = registerUser(login, email, password).shouldDeserializeTo[Register_OUT].apiKey
    RegisteredUser(login, email, password, apiKey)
  }

  def loginUser(loginOrEmail: String, password: String, apiKeyValidHours: Option[Int] = None): Response[Task] = {
    val request = Request[Task](method = POST, uri = uri"/user/login")
      .withEntity(Login_IN(loginOrEmail, password, apiKeyValidHours))

    modules.httpApi.mainRoutes(request).unwrap
  }

  def getUser(apiKey: String): Response[Task] = {
    val request = Request[Task](method = GET, uri = uri"/user")
    modules.httpApi.mainRoutes(authorizedRequest(apiKey, request)).unwrap
  }

  def changePassword(apiKey: String, password: String, newPassword: String): Response[Task] = {
    val request = Request[Task](method = POST, uri = uri"/user/changepassword")
      .withEntity(ChangePassword_IN(password, newPassword))

    modules.httpApi.mainRoutes(authorizedRequest(apiKey, request)).unwrap
  }

  def updateUser(apiKey: String, login: String, email: String): Response[Task] = {
    val request = Request[Task](method = POST, uri = uri"/user")
      .withEntity(UpdateUser_IN(login, email))

    modules.httpApi.mainRoutes(authorizedRequest(apiKey, request)).unwrap
  }

}
