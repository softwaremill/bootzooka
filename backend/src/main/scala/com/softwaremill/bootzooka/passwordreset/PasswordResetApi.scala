package com.softwaremill.bootzooka.passwordreset

import cats.data.NonEmptyList
import com.softwaremill.bootzooka.http.{Error_OUT, Http}
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.util.ServerEndpoints
import doobie.util.transactor.Transactor
import cats.effect.IO
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto._

class PasswordResetApi(http: Http, passwordResetService: PasswordResetService, xa: Transactor[IO]) {
  import PasswordResetApi._
  import http._

  private val PasswordResetPath = "passwordreset"

  val passwordResetEndpoint = baseEndpoint.post
    .in(PasswordResetPath / "reset")
    .in(jsonBody[PasswordReset_IN])
    .out(jsonBody[PasswordReset_OUT])

  private val passwordResetServerEndpoint = passwordResetEndpoint
    .serverLogic { data =>
      (for {
        _ <- passwordResetService.resetPassword(data.code, data.password)
      } yield PasswordReset_OUT()).toOut
    }

  val forgotPasswordEndpoint = baseEndpoint.post
    .in(PasswordResetPath / "forgot")
    .in(jsonBody[ForgotPassword_IN])
    .out(jsonBody[ForgotPassword_OUT])

  private val forgotPasswordServerEndpoint = forgotPasswordEndpoint
    .serverLogic { data =>
      (for {
        _ <- passwordResetService.forgotPassword(data.loginOrEmail).transact(xa)
      } yield ForgotPassword_OUT()).toOut
    }

  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        passwordResetServerEndpoint,
        forgotPasswordServerEndpoint
      )
      .map(_.tag("passwordreset"))
}

object PasswordResetApi {
  case class PasswordReset_IN(code: String, password: String)
  case class PasswordReset_OUT()

  case class ForgotPassword_IN(loginOrEmail: String)
  case class ForgotPassword_OUT()
}
