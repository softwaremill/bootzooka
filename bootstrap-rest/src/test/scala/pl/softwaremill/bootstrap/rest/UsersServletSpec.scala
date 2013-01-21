package pl.softwaremill.bootstrap.rest

import org.specs2.matcher.MatchResult
import pl.softwaremill.bootstrap.service.user.{RegistrationDataValidator, UserService}
import pl.softwaremill.bootstrap.dao.{InMemoryUserDAO, UserDAO}
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.BootstrapServletSpec
import org.json4s.JsonDSL._
import com.mongodb.casbah.Imports._
import pl.softwaremill.bootstrap.common.Utils
import pl.softwaremill.bootstrap.service.schedulers.DummyEmailSendingService
import pl.softwaremill.bootstrap.service.templates.EmailTemplatingEngine
import org.mockito.Matchers

class UsersServletSpec extends BootstrapServletSpec {

  def is = sequential ^ "UserServlet" ^
    "PUT should register new user" ! shouldRegisterNewUser ^
    "PUT with invalid data return error message" ! shouldReturnErrorMessageOnInvalidData ^
    "PUT should use escaped Strings" ! registerShouldUseEscapedStrings ^
    "PATCH should update login when login is given" ! shouldUpdateLoginWhenPresent ^
    "PATCH should not update login when not in request" ! shouldNotUpdateLoginWhenNoLoginPresent ^
    "PATCH should not update login when login is blank" ! shouldNotUpdateLoginWhenBlankLoginIsSent ^
    "PATCH should not update login when user is not authenticated" ! shouldNotUpdateLoginForUnauthenticatedUser ^
    "PATCH should update email when email is given" ! shouldUpdateEmailWhenPresent ^
    "PATCH should not update email when not in request" ! shouldNotUpdateEmailWhenNoEmailPresent ^
    "PATCH should not update email when email is blank" ! shouldNotUpdateEmailWhenBlankEmailIsSent
    "PATCH should not update email when user in not authenticated" ! shouldNotUpdateEmailForUnauthenticatedUser

  end

  var servlet: UsersServlet = _

  def onServletWithMocks(testToExecute: (UserService) => MatchResult[Any]): MatchResult[Any] = {
    val dao = new InMemoryUserDAO
    dao.add(User("Admin", "admin@sml.com", Utils.sha256("pass", "admin")))

    val userService = spy(new UserService(dao, new RegistrationDataValidator(), new DummyEmailSendingService(), new EmailTemplatingEngine))

    servlet = new UsersServlet(userService)
    addServlet(servlet, "/*")

    testToExecute(userService)
  }

  def shouldRegisterNewUser = onServletWithMocks {
    (userService) =>
      post("/register", mapToJson(Map("login" -> "newUser", "email" -> "newUser@sml.com", "password" -> "secret")),
        defaultJsonHeaders) {
        there was one(userService).registerNewUser("newUser", "newUser@sml.com", "secret")
        status must_== 200
      }
  }

  def shouldReturnErrorMessageOnInvalidData = onServletWithMocks {
    (userService) =>
      post("/register", defaultJsonHeaders) {
        val option: Option[String] = (stringToJson(body) \ "value").extractOpt[String]
        option must beEqualTo(Some("Wrong user data!"))
        status must_== 200
      }
  }

  def registerShouldUseEscapedStrings = onServletWithMocks {
    (userService) =>
      post("/register", mapToJson(Map("login" -> "<script>alert('haxor');</script>", "email" -> "newUser@sml.com", "password" -> "secret")), defaultJsonHeaders) {
        there was one(userService).registerNewUser("&lt;script&gt;alert('haxor');&lt;/script&gt;", "newUser@sml.com", "secret")
      }
  }

  def shouldNotUpdateEmailWhenNoEmailPresent = onServletWithMocks(userService => {
    patch("/", mapToJson(Map("irrelevant" -> "")), defaultJsonHeaders) {
      there was no(userService).changeEmail(anyString, anyString)
    }
  })

  def shouldNotUpdateEmailWhenBlankEmailIsSent = onServletWithMocks(userService => {
    patch("/", mapToJson(Map("email" -> "")), defaultJsonHeaders) {
      there was no(userService).changeEmail(anyString, anyString)
    }
  })

  def shouldUpdateEmailWhenPresent = onServletWithMocks(userService => {
    val email = "coolmail@awesome.rox"
    session {
      //authenticate to perform change
      post("/", mapToJson(Map("login" -> "admin", "password" -> "pass")), defaultJsonHeaders) {
        status must be equalTo 200
      }

      patch("/", mapToJson(Map("email" -> email)), defaultJsonHeaders) {
        status must be equalTo 200
        there was one(userService).changeEmail(anyString, Matchers.eq(email))
      }
    }
  })

  def shouldNotUpdateEmailForUnauthenticatedUser = onServletWithMocks(userService => {
    val email = "coolmail@awesome.rox"
    patch("/", mapToJson(Map("email" -> email)), defaultJsonHeaders) {
      status must be equalTo 401
    }
  })

  def shouldNotUpdateLoginWhenNoLoginPresent = onServletWithMocks(userService => {
    patch("/", mapToJson(Map("irrelevant" -> "")), defaultJsonHeaders) {
      there was no(userService).changeLogin(anyString, anyString)
    }
  })

  def shouldNotUpdateLoginWhenBlankLoginIsSent = onServletWithMocks(userService => {
    patch("/", mapToJson(Map("login" -> "")), defaultJsonHeaders) {
      there was no(userService).changeLogin(anyString, anyString)
    }
  })

  def shouldUpdateLoginWhenPresent = onServletWithMocks(userService => {
    val login = "coolNewLogin"
    session {
      //authenticate to perform change
      post("/", mapToJson(Map("login" -> "admin", "password" -> "pass")), defaultJsonHeaders) {
        status must be equalTo 200
      }

      patch("/", mapToJson(Map("login" -> login)), defaultJsonHeaders) {
        status must be equalTo 200
        there was one(userService).changeLogin(anyString, Matchers.eq(login))
      }
    }
  })

  def shouldNotUpdateLoginForUnauthenticatedUser = onServletWithMocks(userService => {
    patch("/", mapToJson(Map("login" -> "admin")), defaultJsonHeaders) {
      status must be equalTo 401
    }
  })


}
