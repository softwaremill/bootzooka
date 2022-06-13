package com.softwaremill.bootzooka.user

import cats.data.NonEmptyList
import cats.effect.IO
import com.softwaremill.bootzooka.http.Http
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.security.{ApiKey, Auth}
import com.softwaremill.bootzooka.util.ServerEndpoints
import doobie.util.transactor.Transactor

import java.time.Instant
import scala.concurrent.duration._

class UserApi(http: Http, auth: Auth[ApiKey], userService: UserService, xa: Transactor[IO]) {
  import UserApi._
  import http._

  private val UserPath = "user"

  private val registerUserEndpoint = baseEndpoint.post
    .in(UserPath / "register")
    .in(jsonBody[Register_IN])
    .out(jsonBody[Register_OUT])
    .serverLogic { data =>
      (for {
        apiKey <- userService.registerNewUser(data.login, data.email, data.password).transact(xa)
        _ <- IO(Metrics.registeredUsersCounter.inc())
      } yield Register_OUT(apiKey.id)).toOut
    }

  private val loginEndpoint = baseEndpoint.post
    .in(UserPath / "login")
    .in(jsonBody[Login_IN])
    .out(jsonBody[Login_OUT])
    .serverLogic { data =>
      (for {
        apiKey <- userService
          .login(data.loginOrEmail, data.password, data.apiKeyValidHours.map(h => Duration(h.toLong, HOURS)))
          .transact(xa)
      } yield Login_OUT(apiKey.id)).toOut
    }

  private val authedEndpoint = secureEndpoint.serverSecurityLogic(authData => auth(authData).toOut)

  private val changePasswordEndpoint = authedEndpoint.post
    .in(UserPath / "changepassword")
    .in(jsonBody[ChangePassword_IN])
    .out(jsonBody[ChangePassword_OUT])
    .serverLogic(id =>
      data =>
        (for {
          _ <- userService.changePassword(id, data.currentPassword, data.newPassword).transact(xa)
        } yield ChangePassword_OUT()).toOut
    )

  private val getUserEndpoint = authedEndpoint.get
    .in(UserPath)
    .out(jsonBody[GetUser_OUT])
    .serverLogic(id =>
      (_: Unit) =>
        (for {
          user <- userService.findById(id).transact(xa)
        } yield GetUser_OUT(user.login, user.emailLowerCased, user.createdOn)).toOut
    )

  private val updateUserEndpoint = authedEndpoint.post
    .in(UserPath)
    .in(jsonBody[UpdateUser_IN])
    .out(jsonBody[UpdateUser_OUT])
    .serverLogic(id =>
      data =>
        (for {
          _ <- userService.changeUser(id, data.login, data.email).transact(xa)
        } yield UpdateUser_OUT()).toOut
    )

  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        registerUserEndpoint,
        loginEndpoint,
        changePasswordEndpoint,
        getUserEndpoint,
        updateUserEndpoint
      )
      .map(_.tag("user"))
}

object UserApi {
  case class Register_IN(login: String, email: String, password: String)
  case class Register_OUT(apiKey: String)

  case class ChangePassword_IN(currentPassword: String, newPassword: String)
  case class ChangePassword_OUT()

  case class Login_IN(loginOrEmail: String, password: String, apiKeyValidHours: Option[Int])
  case class Login_OUT(apiKey: String)

  case class UpdateUser_IN(login: String, email: String)
  case class UpdateUser_OUT()

  case class GetUser_OUT(login: String, email: String, createdOn: Instant)
}
