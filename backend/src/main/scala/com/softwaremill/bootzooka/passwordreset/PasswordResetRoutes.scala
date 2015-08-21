package com.softwaremill.bootzooka.passwordreset

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.softwaremill.bootzooka.api.RoutesSupport
import com.softwaremill.bootzooka.common.StringJsonWrapper
import org.json4s.JValue

trait PasswordResetRoutes extends RoutesSupport {

  def passwordResetService: PasswordResetService

  val passwordResetRoutes = pathPrefix("passwordrecovery") {
    post {
      entity(as[JValue]) { body =>
        path(Segment) { code =>
          (body \ "password").extractOpt[String] match {
            case None => complete(StatusCodes.BadRequest, StringJsonWrapper("missingpassword"))
            case Some(password) =>
              onSuccess(passwordResetService.performPasswordReset(code, password)) {
                case Left(e) => complete(StatusCodes.Forbidden, StringJsonWrapper(e))
                case _ => completeOk
              }
          }
        } ~ delay {
          val loginOrEmail = (body \ "login").extract[String]
          onSuccess(passwordResetService.sendResetCodeToUser(loginOrEmail)) {
            complete(StringJsonWrapper("success"))
          }
        }
      }
    }
  }
}
