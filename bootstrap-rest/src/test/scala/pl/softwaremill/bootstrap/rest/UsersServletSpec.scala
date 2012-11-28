package pl.softwaremill.bootstrap.rest

import org.scalatra.test.specs2.ScalatraSpec
import org.specs2.mock.Mockito
import org.specs2.matcher.{MatchResult, ThrownExpectations}
import pl.softwaremill.bootstrap.service.user.{RegistrationDataValidator, UserService}
import pl.softwaremill.bootstrap.dao.UserDAO
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.BootstrapServletSpec
import org.json4s.JsonAST.JValue
import org.mockito.Matchers

class UsersServletSpec extends BootstrapServletSpec {

  def is = sequential ^ "UserServlet" ^
    "PUT should register new user" ! shouldRegisterNewUser ^
    "PUT with invalid data return error message" ! shouldReturnErrorMessageOnInvalidData

  def onServletWithMocks(testToExecute: (UserService) => MatchResult[Any]): MatchResult[Any] = {
    val dao = mock[UserDAO]
    dao.findByEmail("admin@sml.com") returns Some(new User("admin", "admin@sml.com", "pass"))
    dao.findByEmail("newUser@sml.com") returns None
    dao.findByLogin("admin") returns Some(new User("admin", "admin@sml.com", "pass"))
    dao.findByLogin("newUser") returns None

    val userService = spy(new UserService(dao, new RegistrationDataValidator()))

    val servlet: UsersServlet = new UsersServlet(userService)
    addServlet(servlet, "/*")

    testToExecute(userService)
  }

  def shouldRegisterNewUser = onServletWithMocks{ (userService) =>
    put("/register", mapToJson(Map("login" -> "newUser", "email" -> "newUser@sml.com", "password" -> "secret")), defaultJsonHeaders)  {
      there was one(userService).registerNewUser(Matchers.eq(new User(-1, "newUser", "newUser@sml.com", "secret")))
      status must_== 200
    }
  }

  def shouldReturnErrorMessageOnInvalidData = onServletWithMocks{ (userService) =>
    put("/register", defaultJsonHeaders)  {
      val option: Option[String] = (stringToJson(body) \ "value").extractOpt[String]
      option must beEqualTo(Some("Wrong user data!"))
      status must_== 200
    }
  }

}
