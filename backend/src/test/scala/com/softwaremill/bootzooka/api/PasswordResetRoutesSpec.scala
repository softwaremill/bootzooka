package com.softwaremill.bootzooka.api

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.server.Route
import com.softwaremill.bootzooka.service.PasswordResetService
import com.softwaremill.bootzooka.test.BaseRoutesSpec
import org.json4s.JValue
import org.mockito.BDDMockito._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._

import scala.concurrent.Future

class PasswordResetRoutesSpec extends BaseRoutesSpec {

  def createRoutes(_passwordResetService: PasswordResetService): Route = {
    new PasswordResetRoutes with TestRoutesSupport {
      override val userService = null
      override val passwordResetService = _passwordResetService
    }.passwordResetRoutes
  }

  "POST /" should "send e-mail to user" in {
    // given
    val passwordResetService = mock[PasswordResetService]
    given(passwordResetService.sendResetCodeToUser(any[String])).willReturn(Future.successful(()))
    val routes = createRoutes(passwordResetService)

    // when
    Post("/passwordrecovery", Map("login" -> "mylogin")) ~> routes ~> check {
      valueFromWrapper(responseAs[JValue]) should be ("success")

      verify(passwordResetService).sendResetCodeToUser("mylogin")
    }
  }

  "POST /123 with password" should "change the password" in {
    // given
    val passwordResetService = mock[PasswordResetService]
    given(passwordResetService.performPasswordReset(any[String], any[String])).willReturn(Future.successful(Right(true)))
    val routes = createRoutes(passwordResetService)

    // when
    Post("/passwordrecovery/123", Map("password" -> "validPassword")) ~> routes ~> check {
      valueFromWrapper(responseAs[JValue]) should be ("ok")
      verify(passwordResetService).performPasswordReset("123", "validPassword")
    }
  }

  "POST /123 without password" should "result in an error" in {
    // given
    val passwordResetService = mock[PasswordResetService]
    val routes = createRoutes(passwordResetService)

    // when
    Post("/passwordrecovery/123") ~> routes ~> check {
      valueFromWrapper(responseAs[JValue]) should be ("missingpassword")
      status should be (StatusCodes.BadRequest)
      verify(passwordResetService, never()).performPasswordReset(Matchers.eq("123"), anyString)
    }
  }

  "POST /123 with password but with invalid code" should "result in an error" in {
    // given
    val passwordResetService = mock[PasswordResetService]
    given(passwordResetService.performPasswordReset(any[String], any[String])).willReturn(Future.successful(Left("Error")))
    val routes = createRoutes(passwordResetService)

    // when
    Post("/passwordrecovery/123", Map("password" -> "validPassword")) ~> routes ~> check {
      valueFromWrapper(responseAs[JValue]) should be ("Error")
      status should be (StatusCodes.Forbidden)
    }
  }
}
