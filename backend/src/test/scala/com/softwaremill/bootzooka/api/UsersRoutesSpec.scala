package com.softwaremill.bootzooka.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import akka.http.scaladsl.server.AuthorizationFailedRejection
import com.softwaremill.bootzooka.dao.UserDao
import com.softwaremill.bootzooka.service.email.DummyEmailService
import com.softwaremill.bootzooka.service.templates.EmailTemplatingEngine
import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.test.{BaseRoutesSpec, FlatSpecWithSql, UserTestHelpers}
import org.json4s.{JValue, DefaultFormats}

class UsersRoutesSpec extends BaseRoutesSpec with FlatSpecWithSql with UserTestHelpers { spec =>
  implicit val formats = DefaultFormats

  val userDao = new UserDao(sqlDatabase)
  val userService = new UserService(userDao, new DummyEmailService(), new EmailTemplatingEngine)

  val routes = new UsersRoutes with TestRoutesSupport {
    override val userService = spec.userService
  }.usersRoutes

  override protected def beforeEach() = {
    super.beforeEach()
    userDao.add(newUser("Admin", "admin@sml.com", "pass", "salt", "token1"))
    userDao.add(newUser("Admin2", "admin2@sml.com", "pass", "salt", "token2"))
  }

  "POST /register" should "register new user" in {
    Post("/users/register", Map("login" -> "newUser", "email" -> "newUser@sml.com", "password" -> "secret")) ~> routes ~> check {
      userService.findByLogin("newUser").futureValue should be ('defined)
      status should be (StatusCodes.OK)
    }
  }

  "POST /register with invalid data" should "result in an error" in {
    Post("/users/register") ~> routes ~> check {
      status should be (StatusCodes.InternalServerError)
    }
  }

  "POST /register with an existing login" should "return 409 with an error message" in {
    Post("/users/register", Map("login" -> "Admin", "email" -> "newUser@sml.com", "password" -> "secret")) ~> routes ~> check {
      status should be (StatusCodes.Conflict)
      valueFromWrapper(entityAs[JValue]) should be ("Login already in use!")
    }
  }

  "POST /register with an existing email" should "return 409 with an error message" in {
    Post("/users/register", Map("login" -> "newUser", "email" -> "admin@sml.com", "password" -> "secret")) ~> routes ~> check {
      status should be (StatusCodes.Conflict)
      valueFromWrapper(entityAs[JValue]) should be ("E-mail already in use!")
    }
  }

  "POST /register" should "use escaped Strings" in {
    Post("/users/register", Map("login" -> "<script>alert('haxor');</script>", "email" -> "newUser@sml.com", "password" -> "secret")) ~> routes ~> check {
      status should be (StatusCodes.OK)
      userService.findByEmail("newUser@sml.com").futureValue.map(_.login) should be (Some("&lt;script&gt;alert('haxor');&lt;/script&gt;"))
    }
  }

  def withLoggedInUser(login: String, password: String)(body: RequestTransformer => Unit) = {
    Post("/users", Map("login" -> login, "password" -> password)) ~> routes ~> check {
      status should be (StatusCodes.OK)

      val Some(sessionCookie) = header[`Set-Cookie`]

      body(addHeader(Cookie(sessionConfig.clientSessionCookieConfig.name, sessionCookie.cookie.value)))
    }
  }

  def withAdminLoggedIn(body: RequestTransformer => Unit) = withLoggedInUser("Admin", "pass")(body)

  "POST /" should "log in given valid credentials" in {
    withAdminLoggedIn { _ =>
      // ok
    }
  }

  "POST /" should "not log in given invalid credentials" in {
    Post("/users", Map("login" -> "Admin", "password" -> "hacker")) ~> routes ~> check {
      rejection should be (AuthorizationFailedRejection)
    }
  }

  "PATCH /" should "update email when email is given" in {
    val email = "coolmail@awesome.rox"

    withAdminLoggedIn { transform =>
      Patch("/users", Map("email" -> email)) ~> transform ~> routes ~> check {
        userService.findByLogin("Admin").futureValue.map(_.email) should be(Some(email))
        status should be(StatusCodes.OK)
      }
    }
  }

  "PATCH /" should "update login when login is given" in {
    val login = "Admin3"

    withAdminLoggedIn { transform =>
      Patch("/users", Map("login" -> login)) ~> transform ~> routes ~> check {
        userService.findByLogin(login).futureValue should be ('defined)
        status should be(StatusCodes.OK)
      }
    }
  }

  "PATCH /" should "result in an error when user is not authenticated" in {
    Patch("/users", Map("email" -> "?")) ~> routes ~> check {
      rejection should be (AuthorizationFailedRejection)
    }
  }

  "PATCH /" should "result in an error in neither email nor login is given" in {
    withAdminLoggedIn { transform =>
      Patch("/users") ~> transform ~> routes ~> check {
        status should be (StatusCodes.Conflict)
      }
    }
  }

  "POST /changepassword" should "update password if current is correct and new is present" in {
    withAdminLoggedIn { transform =>
      Post("/users/changepassword", Map("currentPassword" -> "pass", "newPassword" -> "newPass")) ~> transform ~> routes ~> check {
        status should be (StatusCodes.OK)
      }
    }
  }

  "POST /changepassword" should "not update password if current is wrong" in {
    withAdminLoggedIn { transform =>
      Post("/users/changepassword", Map("currentPassword" -> "hacker", "newPassword" -> "newPass")) ~> transform ~> routes ~> check {
        status should be (StatusCodes.Forbidden)
      }
    }
  }
}
