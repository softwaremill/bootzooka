package com.softwaremill.bootzooka.user

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import com.softwaremill.bootzooka.http.Http
import com.softwaremill.bootzooka.infrastructure.DB
import com.softwaremill.bootzooka.infrastructure.Magnum.*
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.security.{ApiKey, Auth}
import com.softwaremill.bootzooka.util.ServerEndpoints
import com.softwaremill.bootzooka.util.Strings.asId
import ox.IO
import sttp.tapir.Schema

import java.time.Instant
import scala.concurrent.duration.*

class UserApi(http: Http, auth: Auth[ApiKey], userService: UserService, db: DB, metrics: Metrics)(using IO):
  import UserApi._
  import http._

  private val UserPath = "user"

  private val registerUserEndpoint = baseEndpoint.post
    .in(UserPath / "register")
    .in(jsonBody[Register_IN])
    .out(jsonBody[Register_OUT])
    .handle { data =>
      val apiKeyResult = db.transactEither(userService.registerNewUser(data.login, data.email, data.password))
      metrics.registeredUsersCounter.add(1)
      apiKeyResult.map(apiKey => Register_OUT(apiKey.id.toString))
    }

  private val loginEndpoint = baseEndpoint.post
    .in(UserPath / "login")
    .in(jsonBody[Login_IN])
    .out(jsonBody[Login_OUT])
    .handle { data =>
      val apiKeyResult =
        db.transactEither(userService.login(data.loginOrEmail, data.password, data.apiKeyValidHours.map(h => Duration(h.toLong, HOURS))))
      apiKeyResult.map(apiKey => Login_OUT(apiKey.id.toString))
    }

  private val authedEndpoint = secureEndpoint.handleSecurity(authData => auth(authData))

  private val logoutEndpoint = authedEndpoint.post
    .in(UserPath / "logout")
    .in(jsonBody[Logout_IN])
    .out(jsonBody[Logout_OUT])
    .handleSuccess { _ => data =>
      db.transactEither(Right(userService.logout(data.apiKey.asId[ApiKey])))
      Logout_OUT()
    }

  private val changePasswordEndpoint = authedEndpoint.post
    .in(UserPath / "changepassword")
    .in(jsonBody[ChangePassword_IN])
    .out(jsonBody[ChangePassword_OUT])
    .handle { id => data =>
      val apiKeyResult = db.transactEither(userService.changePassword(id, data.currentPassword, data.newPassword))
      apiKeyResult.map(apiKey => ChangePassword_OUT(apiKey.id.toString))
    }

  private val getUserEndpoint = authedEndpoint.get
    .in(UserPath)
    .out(jsonBody[GetUser_OUT])
    .handle { id => (_: Unit) =>
      val userResult = db.transactEither(userService.findById(id))
      userResult.map(user => GetUser_OUT(user.login, user.emailLowerCase, user.createdOn))
    }

  private val updateUserEndpoint = authedEndpoint.post
    .in(UserPath)
    .in(jsonBody[UpdateUser_IN])
    .out(jsonBody[UpdateUser_OUT])
    .handle { id => data =>
      db.transactEither(userService.changeUser(id, data.login, data.email)).map(_ => UpdateUser_OUT())
    }

  val endpoints: ServerEndpoints = List(
    registerUserEndpoint,
    loginEndpoint,
    logoutEndpoint,
    changePasswordEndpoint,
    getUserEndpoint,
    updateUserEndpoint
  ).map(_.tag("user"))
end UserApi

object UserApi:
  case class Register_IN(login: String, email: String, password: String) derives ConfiguredJsonValueCodec, Schema
  case class Register_OUT(apiKey: String) derives ConfiguredJsonValueCodec, Schema

  case class ChangePassword_IN(currentPassword: String, newPassword: String) derives ConfiguredJsonValueCodec, Schema
  case class ChangePassword_OUT(apiKey: String) derives ConfiguredJsonValueCodec, Schema

  case class Login_IN(loginOrEmail: String, password: String, apiKeyValidHours: Option[Int]) derives ConfiguredJsonValueCodec, Schema
  case class Login_OUT(apiKey: String) derives ConfiguredJsonValueCodec, Schema

  case class Logout_IN(apiKey: String) derives ConfiguredJsonValueCodec, Schema
  case class Logout_OUT() derives ConfiguredJsonValueCodec, Schema

  case class UpdateUser_IN(login: String, email: String) derives ConfiguredJsonValueCodec, Schema
  case class UpdateUser_OUT() derives ConfiguredJsonValueCodec, Schema

  case class GetUser_OUT(login: String, email: String, createdOn: Instant) derives ConfiguredJsonValueCodec, Schema
end UserApi
