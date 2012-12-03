package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.BootstrapServletSpec
import pl.softwaremill.bootstrap.service.user.UserService
import org.specs2.matcher.MatchResult
import org.specs2.mock.Mockito
import org.scalatra.auth.Scentry
import pl.softwaremill.bootstrap.domain.User
import org.specs2.matcher.MatchResult
import pl.softwaremill.bootstrap.service.data.UserJson

class UserServletWithAuthSpec extends BootstrapServletSpec {

  def is = sequential ^ "UserServlet" ^
    "GET /logout should call logout() when user is already authenticated" ! logoutIfAuthenticated ^
    "GET /logut should not call logout() when user is not authenticated" ! noCallToLogout ^
    "GET should return user information" ! returnInformationAboutLoggedUser


  end

  def onServletWithMocks(authenticated: Boolean, testToExecute: (UserService, Scentry[UserJson])=> MatchResult[Any]): MatchResult[Any] = {
    val userService = mock[UserService]

    val mockedScentry = mock[Scentry[UserJson]]
    mockedScentry.isAuthenticated returns authenticated

    val servlet: MockUsersServlet = new MockUsersServlet(userService, mockedScentry)
    addServlet(servlet, "/*")

    testToExecute(userService, mockedScentry)
  }

  def logoutIfAuthenticated = onServletWithMocks(authenticated = true, testToExecute = (userService, mock) =>
    get("/logout") {
      there was two(mock).isAuthenticated // before() and get('/logout')
      there was one(mock).logout()
      there was noCallsTo(userService)
    }
  )

  def noCallToLogout = onServletWithMocks(authenticated = false, testToExecute = (userService, mock) =>
      get("/logout") {
        there was two(mock).isAuthenticated // before() and get('/logout')
        there was no(mock).logout()
        there was noCallsTo(userService)
      }
  )

  def returnInformationAboutLoggedUser = onServletWithMocks(authenticated = true, testToExecute = (userService, mock) =>
    get("/") {
      status must_== 200
      body mustEqual("{\"login\":\"Jas Kowalski\",\"token\":\"token\"}")
    }
  )

}

class MockUsersServlet(userService: UserService, mockedScentry: Scentry[UserJson]) extends UsersServlet(userService) with Mockito {

  override def scentry = {
    mockedScentry
  }

  override def user = new UserJson("Jas Kowalski", "token")

}