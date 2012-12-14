package pl.softwaremill.bootstrap.rest

import org.specs2.matcher.MatchResult
import pl.softwaremill.bootstrap.service.user.{RegistrationDataValidator, UserService}
import pl.softwaremill.bootstrap.dao.UserDAO
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.BootstrapServletSpec
import org.json4s.JsonDSL._
import com.mongodb.casbah.Imports._
import pl.softwaremill.bootstrap.common.Utils

class UsersServletSpec extends BootstrapServletSpec {

  def is = sequential ^ "UserServlet" ^
    "PUT should register new user"                  ! shouldRegisterNewUser ^
    "PUT with invalid data return error message"    ! shouldReturnErrorMessageOnInvalidData ^
    "PUT should use escaped Strings"                ! registerShouldUseEscapedStrings
  end

  def onServletWithMocks(testToExecute: (UserService) => MatchResult[Any]): MatchResult[Any] = {
    val dao = mock[UserDAO]
    dao.findByEmail("admin@sml.com") returns Some(new User(new ObjectId("a" * 24), "Admin", "admin", "admin@sml.com", "pass",
      Utils.sha256("pass", "admin")))
    dao.findByEmail("newUser@sml.com") returns None
    dao.findByLogin("admin") returns Some(User("admin", "admin@sml.com", "pass"))
    dao.findByLogin("newUser") returns None

    val userService = spy(new UserService(dao, new RegistrationDataValidator()))

    val servlet: UsersServlet = new UsersServlet(userService)
    addServlet(servlet, "/*")

    testToExecute(userService)
  }

  def shouldRegisterNewUser = onServletWithMocks{ (userService) =>
    post("/register", mapToJson(Map("login" -> "newUser", "email" -> "newUser@sml.com", "password" -> "secret")),
      defaultJsonHeaders)  {
        there was one(userService).registerNewUser("newUser", "newUser@sml.com", "secret")
        status must_== 200
    }
  }

  def shouldReturnErrorMessageOnInvalidData = onServletWithMocks{ (userService) =>
    post("/register", defaultJsonHeaders)  {
      val option: Option[String] = (stringToJson(body) \ "value").extractOpt[String]
      option must beEqualTo(Some("Wrong user data!"))
      status must_== 200
    }
  }

  def registerShouldUseEscapedStrings = onServletWithMocks{(userService) =>
    post("/register", mapToJson(Map("login" -> "<script>alert('haxor');</script>", "email" -> "newUser@sml.com", "password" -> "secret")), defaultJsonHeaders) {
      there was one(userService).registerNewUser("&lt;script&gt;alert('haxor');&lt;/script&gt;", "newUser@sml.com", "secret")
    }
  }

}
