package com.softwaremill.bootzooka.api

import com.softwaremill.bootzooka.BaseServletSpec
import com.softwaremill.bootzooka.dao.UserDao
import com.softwaremill.bootzooka.service.email.DummyEmailService
import com.softwaremill.bootzooka.service.templates.EmailTemplatingEngine
import com.softwaremill.bootzooka.service.user.{RegistrationDataValidator, UserService}
import com.softwaremill.bootzooka.test.{FlatSpecWithSql, UserTestHelpers}
import org.json4s.JsonDSL._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext.Implicits.global

class UsersServletSpec extends BaseServletSpec with FlatSpecWithSql with UserTestHelpers {
  var servlet: UsersServlet = _

  def onServletWithMocks(testToExecute: (UserService) => Unit) = {
    val dao = new UserDao(sqlDatabase)
    dao.add(newUser("Admin", "admin@sml.com", "pass", "salt", "token1"))
    dao.add(newUser("Admin2", "admin2@sml.com", "pass", "salt", "token2"))

    val userService = spy(new UserService(dao, new RegistrationDataValidator(), new DummyEmailService(), new EmailTemplatingEngine))

    servlet = new UsersServlet(userService)
    addServlet(servlet, "/*")

    testToExecute(userService)
  }

  "POST /register" should "register new user" in {
    onServletWithMocks {
      (userService) =>
        post("/register", mapToJson(Map("login" -> "newUser", "email" -> "newUser@sml.com", "password" -> "secret")),
          defaultJsonHeaders) {
          verify(userService).registerNewUser("newUser", "newUser@sml.com", "secret")
          status should be (201)
        }
    }
  }

  "POST /register with invalid data" should "return 400 with an error message" in {
    onServletWithMocks {
      (userService) =>
        post("/register", defaultJsonHeaders) {
          val option: Option[String] = (stringToJson(body) \ "value").extractOpt[String]
          option should be(Some("Wrong user data!"))
          status should be(400)
        }
    }
  }

  "POST /register with an existing login" should "return 409 with an error message" in {
    onServletWithMocks {
      (userService) =>
        {
          val newUserWithExistingLogin = mapToJson(Map(
            "login" -> "Admin",
            "email" -> "newUser@sml.com",
            "password" -> "secret"
          ))

          post("/register", newUserWithExistingLogin, defaultJsonHeaders) {
            val option: Option[String] = (stringToJson(body) \ "value").extractOpt[String]
            option should be(Some("Login already in use!"))
            status should be(409)
          }
        }
    }
  }

  "POST /register with an existing email" should "return 409 with an error message" in {
    onServletWithMocks {
      (userService) =>
        {
          val newUserWithExistingEmail = mapToJson(Map(
            "login" -> "newUser",
            "email" -> "admin@sml.com",
            "password" -> "secret"
          ))

          post("/register", newUserWithExistingEmail, defaultJsonHeaders) {
            val option: Option[String] = (stringToJson(body) \ "value").extractOpt[String]
            option should be(Some("E-mail already in use!"))
            status should be(409)
          }
        }
    }
  }

  "POST /register" should "use escaped Strings" in {
    onServletWithMocks {
      (userService) =>
        post("/register", mapToJson(Map("login" -> "<script>alert('haxor');</script>", "email" -> "newUser@sml.com", "password" -> "secret")), defaultJsonHeaders) {
          verify(userService).registerNewUser("&lt;script&gt;alert('haxor');&lt;/script&gt;", "newUser@sml.com", "secret")
        }
    }
  }

  "PATCH /" should "not update email when not in request" in {
    onServletWithMocks(userService => {
      patch("/", mapToJson(Map("irrelevant" -> "")), defaultJsonHeaders) {
        verify(userService, never()).changeEmail(anyString, anyString)
      }
    })
  }

  "PATCH /" should "not update email when email is blank" in {
    onServletWithMocks(userService => {
      patch("/", mapToJson(Map("email" -> "")), defaultJsonHeaders) {
        verify(userService, never()).changeEmail(anyString, anyString)
      }
    })
  }

  "PATCH /" should "update email when email is given" in {
    onServletWithMocks(userService => {
      val email = "coolmail@awesome.rox"
      session {
        //authenticate to perform change
        post("/", mapToJson(Map("login" -> "admin", "password" -> "pass")), defaultJsonHeaders) {
          status should be (200)
        }

        patch("/", mapToJson(Map("email" -> email)), defaultJsonHeaders) {
          status should be (204)
          verify(userService).changeEmail(anyString, Matchers.eq(email))
        }
      }
    })
  }

  "PATCH /" should "complain when user is not authenticated" in {
    onServletWithMocks(userService => {
      val email = "coolmail@awesome.rox"
      patch("/", mapToJson(Map("email" -> email)), defaultJsonHeaders) {
        status should be (401)
      }
    })
  }

  "PATCH /" should "not update email when it's used by another user" in {
    onServletWithMocks(userService => {
      session {
        //authenticate to perform change
        post("/", mapToJson(Map("login" -> "admin", "password" -> "pass")), defaultJsonHeaders) {
          status should be (200)
        }
        patch("/", mapToJson(Map("email" -> "admin2@sml.com")), defaultJsonHeaders) {
          val opt = (stringToJson(body) \ "value").extractOpt[String]
          status should be (409)
          opt should be (Some("E-mail used by another user"))
        }
      }
    })
  }

  "PATCH /" should "not update login when not in request" in {
    onServletWithMocks(userService => {
      patch("/", mapToJson(Map("irrelevant" -> "")), defaultJsonHeaders) {
        verify(userService, never()).changeLogin(anyString, anyString)
      }
    })
  }

  "PATCH /" should "not update login when login is blank" in {
    onServletWithMocks(userService => {
      patch("/", mapToJson(Map("login" -> "")), defaultJsonHeaders) {
        verify(userService, never()).changeLogin(anyString, anyString)
      }
    })
  }

  "PATCH /" should "update login when login is given" in {
    onServletWithMocks(userService => {
      val login = "coolNewLogin"
      session {
        //authenticate to perform change
        post("/", mapToJson(Map("login" -> "admin", "password" -> "pass")), defaultJsonHeaders) {
          status should be (200)
        }

        patch("/", mapToJson(Map("login" -> login)), defaultJsonHeaders) {
          status should be (204)
          verify(userService).changeLogin(anyString, Matchers.eq(login))
        }
      }
    })
  }

  "PATCH /" should "not update login when already used by another user" in {
    onServletWithMocks(userService => {
      session {
        //authenticate to perform change
        post("/", mapToJson(Map("login" -> "admin", "password" -> "pass")), defaultJsonHeaders) {
          status should be (200)
        }
        patch("/", mapToJson(Map("login" -> "admin2")), defaultJsonHeaders) {
          val opt = (stringToJson(body) \ "value").extractOpt[String]
          status should be (409)
          opt should be (Some("Login is already taken"))
        }
      }
    })
  }

  "POST /changepassword" should "update password if current is correct and new is present" in {
    onServletWithMocks(userService => {
      session {
        //authenticate to perform change
        post("/", mapToJson(Map("login" -> "admin", "password" -> "pass")), defaultJsonHeaders) {
          status should be (200)
        }

        post("/changepassword", mapToJson(Map("currentPassword" -> "pass", "newPassword" -> "newPass")), defaultJsonHeaders) {
          status should be (204)
        }
      }
    })
  }

  "POST /changepassword" should "not update password if current is wrong" in {
    onServletWithMocks(userService => {
      session {
        //authenticate to perform change
        post("/", mapToJson(Map("login" -> "admin", "password" -> "pass")), defaultJsonHeaders) {
          status should be (200)
        }

        post("/changepassword", mapToJson(Map("currentPassword" -> "passwrong", "newPassword" -> "newPass")), defaultJsonHeaders) {
          val opt = (stringToJson(body) \ "value").extractOpt[String]
          status should be (403)
          opt should be (Some("Current password is invalid"))
        }
      }
    })
  }

  "POST /changepassword" should "not update password if current is missing" in {
    onServletWithMocks(userService => {
      session {
        //authenticate to perform change
        post("/", mapToJson(Map("login" -> "admin", "password" -> "pass")), defaultJsonHeaders) {
          status should be (200)
        }

        post("/changepassword", mapToJson(Map("newPassword" -> "pass")), defaultJsonHeaders) {
          val opt = (stringToJson(body) \ "value").extractOpt[String]
          status should be (400)
          opt should be (Some("Parameter currentPassword is missing"))
        }
      }
    })
  }

  "POST /changepassword" should "not update password if current is correct but new is missing" in {
    onServletWithMocks(userService => {
      session {
        //authenticate to perform change
        post("/", mapToJson(Map("login" -> "admin", "password" -> "pass")), defaultJsonHeaders) {
          status should be (200)
        }

        post("/changepassword", mapToJson(Map("currentPassword" -> "pass")), defaultJsonHeaders) {
          val opt = (stringToJson(body) \ "value").extractOpt[String]
          status should be (400)
          opt should be (Some("Parameter newPassword is missing"))
        }
      }
    })
  }
}
