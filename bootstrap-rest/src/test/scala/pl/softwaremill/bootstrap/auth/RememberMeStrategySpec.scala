package pl.softwaremill.bootstrap.auth

import org.scalatra.SweetCookies
import org.specs2.mock.Mockito
import javax.servlet.http.HttpServletResponse
import pl.softwaremill.bootstrap.rest.EntriesServlet
import pl.softwaremill.bootstrap.service.user.UserService
import pl.softwaremill.bootstrap.service.data.UserJson
import org.scalatra.test.scalatest.ScalatraFlatSpec

class RememberMeStrategySpec extends ScalatraFlatSpec with Mockito {
  behavior of "RememberMe"

  val httpResponse = mock[HttpServletResponse]
  val app = mock[EntriesServlet]
  val userService = mock[UserService]
  val loggedUser: UserJson = UserJson("admin", "admin@admin.net", "token")
  userService.authenticateWithToken(loggedUser.token) returns Option(loggedUser)

  val rememberMe = true
  val strategy = new RememberMeStrategy(app, rememberMe, userService)

  it should "authenticate user base on cookie" in {
    // Given
    app.cookies returns new SweetCookies(Map(("rememberMe", loggedUser.token)), httpResponse)

    // When
    val user: Option[UserJson] = strategy.authenticate()

    // Then
    user must not be (None)
    user.get.login must be ("admin")
  }

  it should "not authenticate user with invalid cookie" in {
    // Given
    app.cookies returns new SweetCookies(Map(("rememberMe", loggedUser.token + "X")), httpResponse)

    // When
    val user: Option[UserJson] = strategy.authenticate()

    // Then
    user must be (null)
  }
}
