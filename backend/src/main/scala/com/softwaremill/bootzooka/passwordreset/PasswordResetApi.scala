package com.softwaremill.bootzooka.passwordreset

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import com.softwaremill.bootzooka.http.Http.*
import com.softwaremill.bootzooka.infrastructure.DB
import com.softwaremill.bootzooka.infrastructure.Magnum.*
import com.softwaremill.bootzooka.http.{EndpointsForDocs, ServerEndpoints}
import ox.IO
import sttp.tapir.Schema

class PasswordResetApi(passwordResetService: PasswordResetService, db: DB)(using IO) extends ServerEndpoints:
  import PasswordResetApi._

  private val passwordResetServerEndpoint = passwordResetEndpoint.handle { data =>
    passwordResetService.resetPassword(data.code, data.password).map(_ => PasswordReset_OUT())
  }

  private val forgotPasswordServerEndpoint = forgotPasswordEndpoint.handleSuccess { data =>
    db.transact(passwordResetService.forgotPassword(data.loginOrEmail))
    ForgotPassword_OUT()
  }

  override val endpoints = List(
    passwordResetServerEndpoint,
    forgotPasswordServerEndpoint
  )

object PasswordResetApi extends EndpointsForDocs:
  private val PasswordResetPath = "passwordreset"

  private val passwordResetEndpoint = baseEndpoint.post
    .in(PasswordResetPath / "reset")
    .in(jsonBody[PasswordReset_IN])
    .out(jsonBody[PasswordReset_OUT])

  private val forgotPasswordEndpoint = baseEndpoint.post
    .in(PasswordResetPath / "forgot")
    .in(jsonBody[ForgotPassword_IN])
    .out(jsonBody[ForgotPassword_OUT])

  override val endpointsForDocs = List(
    passwordResetEndpoint,
    forgotPasswordEndpoint
  ).map(_.tag("passwordreset"))

  //

  case class PasswordReset_IN(code: String, password: String) derives ConfiguredJsonValueCodec, Schema
  case class PasswordReset_OUT() derives ConfiguredJsonValueCodec, Schema

  case class ForgotPassword_IN(loginOrEmail: String) derives ConfiguredJsonValueCodec, Schema
  case class ForgotPassword_OUT() derives ConfiguredJsonValueCodec, Schema
