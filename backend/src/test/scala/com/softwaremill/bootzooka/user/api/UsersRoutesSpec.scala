package com.softwaremill.bootzooka.user.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import akka.http.scaladsl.server.Route
import com.softwaremill.bootzooka.test.{BaseRoutesSpec, TestHelpersWithDb}

class UsersRoutesSpec extends BaseRoutesSpec with TestHelpersWithDb { spec =>

  val routes = Route.seal(new UsersRoutes with TestRoutesSupport {
    override val userService = spec.userService
  }.usersRoutes)

  "POST /register" should "register new user" in {
    Post("/users/register", Map("login" -> "newUser", "email" -> "newUser@sml.com", "password" -> "secret")) ~> routes ~> check {
      userDao.findByLowerCasedLogin("newUser").futureValue should be ('defined)
      status should be (StatusCodes.OK)
    }
  }

  "POST /register with invalid data" should "result in an error" in {
    Post("/users/register") ~> routes ~> check {
      status should be (StatusCodes.BadRequest)
    }
  }

  "POST /users/whatever" should "not be bound to /users login - reject unmatchedPath request" in {
    Post("/users/whatever") ~> routes ~> check {
      status should be (StatusCodes.NotFound)
    }
  }

  "POST /register with an existing login" should "return 409 with an error message" in {
    userDao.add(newUser("user1", "user1@sml.com", "pass", "salt")).futureValue
    Post("/users/register", Map("login" -> "user1", "email" -> "newUser@sml.com", "password" -> "secret")) ~> routes ~> check {
      status should be (StatusCodes.Conflict)
      entityAs[String] should be ("Login already in use!")
    }
  }

  "POST /register with an existing email" should "return 409 with an error message" in {
    userDao.add(newUser("user2", "user2@sml.com", "pass", "salt")).futureValue
    Post("/users/register", Map("login" -> "newUser", "email" -> "user2@sml.com", "password" -> "secret")) ~> routes ~> check {
      status should be (StatusCodes.Conflict)
      entityAs[String] should be ("E-mail already in use!")
    }
  }

  "POST /register" should "use escaped Strings" in {
    Post("/users/register", Map("login" -> "<script>alert('haxor');</script>", "email" -> "newUser@sml.com", "password" -> "secret")) ~> routes ~> check {
      status should be (StatusCodes.OK)
      userDao.findByEmail("newUser@sml.com").futureValue.map(_.login) should be (Some("&lt;script&gt;alert('haxor');&lt;/script&gt;"))
    }
  }

  def withLoggedInUser(login: String, password: String)(body: RequestTransformer => Unit) = {
    Post("/users", Map("login" -> login, "password" -> password)) ~> routes ~> check {
      status should be (StatusCodes.OK)

      val Some(sessionCookie) = header[`Set-Cookie`]

      body(addHeader(Cookie(sessionConfig.sessionCookieConfig.name, sessionCookie.cookie.value)))
    }
  }

  "POST /" should "log in given valid credentials" in {
    userDao.add(newUser("user3", "user3@sml.com", "pass", "salt")).futureValue
    withLoggedInUser("user3", "pass") { _ =>
      // ok
    }
  }

  "POST /" should "not log in given invalid credentials" in {
    userDao.add(newUser("user4", "user4@sml.com", "pass", "salt")).futureValue
    Post("/users", Map("login" -> "user4", "password" -> "hacker")) ~> routes ~> check {
      status should be (StatusCodes.Forbidden)
    }
  }

  "PATCH /" should "update email when email is given" in {
    userDao.add(newUser("user5", "user5@sml.com", "pass", "salt")).futureValue
    val email = "coolmail@awesome.rox"

    withLoggedInUser("user5", "pass") { transform =>
      Patch("/users", Map("email" -> email)) ~> transform ~> routes ~> check {
        userDao.findByLowerCasedLogin("user5").futureValue.map(_.email) should be(Some(email))
        status should be (StatusCodes.OK)
      }
    }
  }

  "PATCH /" should "update login when login is given" in {
    userDao.add(newUser("user6", "user6@sml.com", "pass", "salt")).futureValue
    val login = "user6_changed"

    withLoggedInUser("user6", "pass") { transform =>
      Patch("/users", Map("login" -> login)) ~> transform ~> routes ~> check {
        userDao.findByLowerCasedLogin(login).futureValue should be ('defined)
        status should be(StatusCodes.OK)
      }
    }
  }

  "PATCH /" should "result in an error when user is not authenticated" in {
    Patch("/users", Map("email" -> "?")) ~> routes ~> check {
      status should be (StatusCodes.Forbidden)
    }
  }

  "PATCH /" should "result in an error in neither email nor login is given" in {
    userDao.add(newUser("user7", "user7@sml.com", "pass", "salt")).futureValue
    withLoggedInUser("user7", "pass") { transform =>
      Patch("/users", Map.empty[String, String]) ~> transform ~> routes ~> check {
        status should be (StatusCodes.Conflict)
      }
    }
  }

  "POST /changepassword" should "update password if current is correct and new is present" in {
    userDao.add(newUser("user8", "user8@sml.com", "pass", "salt")).futureValue
    withLoggedInUser("user8", "pass") { transform =>
      Post("/users/changepassword", Map("currentPassword" -> "pass", "newPassword" -> "newPass")) ~> transform ~> routes ~> check {
        status should be (StatusCodes.OK)
      }
    }
  }

  "POST /changepassword" should "not update password if current is wrong" in {
    userDao.add(newUser("user9", "user9@sml.com", "pass", "salt")).futureValue
    withLoggedInUser("user9", "pass") { transform =>
      Post("/users/changepassword", Map("currentPassword" -> "hacker", "newPassword" -> "newPass")) ~> transform ~> routes ~> check {
        status should be (StatusCodes.Forbidden)
      }
    }
  }
}
