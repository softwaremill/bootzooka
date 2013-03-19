package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.BootstrapServletSpec
import com.softwaremill.bootzooka.service.user.UserService
import org.scalatra.auth.Scentry
import com.softwaremill.bootzooka.service.data.UserJson
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

class UsersServletWithAuthSpec extends BootstrapServletSpec {

  def onServletWithMocks(authenticated: Boolean, testToExecute: (UserService, Scentry[UserJson]) => Unit) {
    val userService = mock[UserService]

    val mockedScentry = mock[Scentry[UserJson]]
    when(mockedScentry.isAuthenticated) thenReturn authenticated

    val servlet: MockUsersServlet = new MockUsersServlet(userService, mockedScentry)
    addServlet(servlet, "/*")

    testToExecute(userService, mockedScentry)
  }

  "GET /logout" should "call logout() when user is already authenticated" in {
    onServletWithMocks(authenticated = true, testToExecute = (userService, mock) =>
      get("/logout") {
        verify(mock, times(2)).isAuthenticated // before() and get('/logout')
        verify(mock).logout()
        verifyZeroInteractions(userService)
      }
    )
  }

  "GET /logout" should "not call logout() when user is not authenticated" in {
    onServletWithMocks(authenticated = false, testToExecute = (userService, mock) =>
      get("/logout") {
        verify(mock, times(2)).isAuthenticated // before() and get('/logout')
        verify(mock, never).logout()
        verifyZeroInteractions(userService)
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

  class MockUsersServlet(userService: UserService, mockedScentry: Scentry[UserJson]) extends UsersServlet(userService) with MockitoSugar {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = mockedScentry
    override def user(implicit request: javax.servlet.http.HttpServletRequest) = new UserJson("Jas Kowalski", "kowalski@kowalski.net", "token")
  }
}

