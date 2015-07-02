package com.softwaremill.bootzooka.auth

import java.util.UUID
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.softwaremill.bootzooka.api.UsersServlet
import com.softwaremill.bootzooka.service.data.UserJson
import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.test.UserTestHelpers
import org.mockito.BDDMockito._
import org.scalatest.mock.MockitoSugar
import org.scalatra.SweetCookies
import org.scalatra.test.scalatest.ScalatraFlatSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RememberMeStrategySpec extends ScalatraFlatSpec with MockitoSugar with UserTestHelpers {
  behavior of "RememberMe"

  implicit val httpResponse = mock[HttpServletResponse]
  implicit val httpRequest = mock[HttpServletRequest]
  val app = mock[UsersServlet]
  val userService = mock[UserService]
  val loggedUser: UserJson = UserJson(UUID.fromString(uuidStr), "admin", "admin@admin.net", "token", createdOn)

  val rememberMe = true
  val strategy = new RememberMeStrategy(app, rememberMe, userService)

  it should "authenticate user base on cookie" in {
    // Given
    given(app.cookies) willReturn new SweetCookies(Map(("rememberMe", loggedUser.token)), httpResponse)
    given(userService.authenticateWithToken(loggedUser.token)) willReturn Future { Some(loggedUser) }

    // When
    val user = strategy.authenticate()

    // Then
    user should not be None
    user.get.login should be ("admin")
  }

  it should "not authenticate user with invalid cookie" in {
    // Given
    given(app.cookies) willReturn new SweetCookies(Map(("rememberMe", loggedUser.token + "X")), httpResponse)
    given(userService.authenticateWithToken(loggedUser.token + "X")) willReturn Future { None }

    // When
    val user = strategy.authenticate()

    // Then
    user should be (None)
  }
}
