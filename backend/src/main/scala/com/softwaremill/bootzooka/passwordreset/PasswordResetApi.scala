package com.softwaremill.bootzooka.passwordreset

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import com.softwaremill.bootzooka.http.Http
import com.softwaremill.bootzooka.infrastructure.DB
import com.softwaremill.bootzooka.infrastructure.Magnum.*
import com.softwaremill.bootzooka.util.ServerEndpoints
import sttp.tapir.Schema

class PasswordResetApi(http: Http, passwordResetService: PasswordResetService, db: DB):
  import PasswordResetApi._
  import http._

  private val PasswordResetPath = "passwordreset"

  private val passwordResetEndpoint = baseEndpoint.post
    .in(PasswordResetPath / "reset")
    .in(jsonBody[PasswordReset_IN])
    .out(jsonBody[PasswordReset_OUT])
    .handle { data =>
      passwordResetService.resetPassword(data.code, data.password).map(_ => PasswordReset_OUT())
    }

  private val forgotPasswordEndpoint = baseEndpoint.post
    .in(PasswordResetPath / "forgot")
    .in(jsonBody[ForgotPassword_IN])
    .out(jsonBody[ForgotPassword_OUT])
    .handleSuccess { data =>
      db.transact(passwordResetService.forgotPassword(data.loginOrEmail))
      ForgotPassword_OUT()
    }

  val endpoints: ServerEndpoints = List(
    passwordResetEndpoint,
    forgotPasswordEndpoint
  ).map(_.tag("passwordreset"))

object PasswordResetApi:
  case class PasswordReset_IN(code: String, password: String) derives ConfiguredJsonValueCodec, Schema
  case class PasswordReset_OUT() derives ConfiguredJsonValueCodec, Schema

  case class ForgotPassword_IN(loginOrEmail: String) derives ConfiguredJsonValueCodec, Schema
  case class ForgotPassword_OUT() derives ConfiguredJsonValueCodec, Schema
