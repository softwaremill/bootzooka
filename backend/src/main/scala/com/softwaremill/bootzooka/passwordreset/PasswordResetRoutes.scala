package com.softwaremill.bootzooka.passwordreset

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.softwaremill.bootzooka.api.RoutesSupport
import com.softwaremill.bootzooka.common.StringJsonWrapper
import org.json4s.JValue

trait PasswordResetRoutes extends RoutesSupport {

  def passwordResetService: PasswordResetService

  val passwordResetRoutes = pathPrefix("passwordreset") {
    post {
      path(Segment) { code =>
        entity(as[PasswordResetInput]) { in =>
          onSuccess(passwordResetService.performPasswordReset(code, in.password)) {
            case Left(e) => complete(StatusCodes.Forbidden, StringJsonWrapper(e))
            case _ => completeOk
          }
        }
      } ~ entity(as[ForgotPasswordInput]) { in =>
        {
          onSuccess(passwordResetService.sendResetCodeToUser(in.login)) {
            complete(StringJsonWrapper("success"))
          }
        }
      }
    }
  }
}

case class PasswordResetInput(password: String)

case class ForgotPasswordInput(login: String)