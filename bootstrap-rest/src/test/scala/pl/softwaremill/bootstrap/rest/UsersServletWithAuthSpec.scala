package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.BootstrapServletSpec
import pl.softwaremill.bootstrap.service.user.UserService
import org.specs2.mock.Mockito
import org.scalatra.auth.Scentry
import pl.softwaremill.bootstrap.service.data.UserJson

class UsersServletWithAuthSpec extends BootstrapServletSpec {

  def onServletWithMocks(authenticated: Boolean, testToExecute: (UserService, Scentry[UserJson]) => Unit) {
    val userService = mock[UserService]

    val mockedScentry = mock[Scentry[UserJson]]
    mockedScentry.isAuthenticated returns authenticated

    val servlet: MockUsersServlet = new MockUsersServlet(userService, mockedScentry)
    addServlet(servlet, "/*")

    testToExecute(userService, mockedScentry)
  }

  "GET /logout" should "call logout() when user is already authenticated" in {
    onServletWithMocks(authenticated = true, testToExecute = (userService, mock) =>
      get("/logout") {
        there was two(mock).isAuthenticated // before() and get('/logout')
        there was one(mock).logout()
        there was noCallsTo(userService)
      }
    )
  }

  "GET /logout" should "not call logout() when user is not authenticated" in {
    onServletWithMocks(authenticated = false, testToExecute = (userService, mock) =>
      get("/logout") {
        there was two(mock).isAuthenticated // before() and get('/logout')
        there was no(mock).logout()
        there was noCallsTo(userService)
      }
    )
  }

  "GET /" should "return user information" in {
    onServletWithMocks(authenticated = true, testToExecute = (userService, mock) =>
      get("/") {
        status should be (200)
        body should be ("{\"login\":\"Jas Kowalski\",\"email\":\"kowalski@kowalski.net\",\"token\":\"token\"}")
      }
    )
  }

  class MockUsersServlet(userService: UserService, mockedScentry: Scentry[UserJson]) extends UsersServlet(userService) with Mockito {
    override def scentry = mockedScentry
    override def user = new UserJson("Jas Kowalski", "kowalski@kowalski.net", "token")
  }
}

