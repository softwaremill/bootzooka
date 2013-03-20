package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.service.user.{RegistrationDataValidator, UserService}
import com.softwaremill.bootzooka.dao.InMemoryUserDAO
import com.softwaremill.bootzooka.domain.User
import com.softwaremill.bootzooka.BootzookaServletSpec
import org.json4s.JsonDSL._
import com.softwaremill.bootzooka.service.schedulers.DummyEmailSendingService
import com.softwaremill.bootzooka.service.templates.EmailTemplatingEngine
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.Matchers

class UsersServletSpec extends BootzookaServletSpec {
  var servlet: UsersServlet = _

  def onServletWithMocks(testToExecute: (UserService) => Unit) = {
    val dao = new InMemoryUserDAO
    dao.add(User("Admin", "admin@sml.com", "pass", "salt", "token1"))
    dao.add(User("Admin2", "admin2@sml.com", "pass", "salt", "token2"))

    val userService = spy(new UserService(dao, new RegistrationDataValidator(), new DummyEmailSendingService(), new EmailTemplatingEngine))

    servlet = new UsersServlet(userService)
    addServlet(servlet, "/*")

    testToExecute(userService)
  }

  "POST /" should "register new user" in {
    onServletWithMocks {
      (userService) =>
        post("/register", mapToJson(Map("login" -> "newUser", "email" -> "newUser@sml.com", "password" -> "secret")),
          defaultJsonHeaders) {
          verify(userService).registerNewUser("newUser", "newUser@sml.com", "secret")
          status should be (200)
        }
    }
  }

  "POST / with invalid data" should "return error message" in {
    onServletWithMocks {
      (userService) =>
        post("/register", defaultJsonHeaders) {
          val option: Option[String] = (stringToJson(body) \ "value").extractOpt[String]
          option should be(Some("Wrong user data!"))
          status should be (200)
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
          status should be (200)
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
          status should be (403)
          opt must be (Some("E-mail used by another user"))
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
          status should be (200)
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
          status should be (403)
          opt must be (Some("Login is already taken"))
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
          status should be (200)
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
          opt must be (Some("Current password is invalid"))
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
          status should be (403)
          opt must be (Some("Parameter currentPassword is missing"))
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
          status should be (403)
          opt must be (Some("Parameter newPassword is missing"))
        }
      }
    })
  }

}
