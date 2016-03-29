package com.softwaremill.bootzooka.passwordreset

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.softwaremill.bootzooka.test.{BaseRoutesSpec, TestHelpersWithDb}
import com.softwaremill.bootzooka.user.User

class PasswordResetRoutesSpec extends BaseRoutesSpec with TestHelpersWithDb { spec =>

  val passwordResetCodeDao = new PasswordResetCodeDao(sqlDatabase)
  val passwordResetService = new PasswordResetService(userDao, passwordResetCodeDao, emailService, emailTemplatingEngine, config)

  val routes = Route.seal(new PasswordResetRoutes with TestRoutesSupport {
    override val userService = spec.userService
    override val passwordResetService = spec.passwordResetService
  }.passwordResetRoutes)

  "POST /" should "send e-mail to user" in {
    // given
    val user = newRandomStoredUser()

    // when
    Post("/passwordreset", Map("login" -> user.login)) ~> routes ~> check {
      emailService.wasEmailSentTo(user.email) should be (true)
    }
  }

  "POST /123 with password" should "change the password" in {
    // given
    val user = newRandomStoredUser()
    val code = PasswordResetCode(randomString(), user)
    passwordResetCodeDao.add(code).futureValue

    val newPassword = randomString()

    // when
    Post(s"/passwordreset/${code.code}", Map("password" -> newPassword)) ~> routes ~> check {
      responseAs[String] should be ("ok")
      User.passwordsMatch(newPassword, userDao.findById(user.id).futureValue.get) should be (true)
    }
  }

  "POST /123 without password" should "result in an error" in {
    // given
    val user = newRandomStoredUser()
    val code = PasswordResetCode(randomString(), user)
    passwordResetCodeDao.add(code).futureValue

    // when
    Post("/passwordreset/123") ~> routes ~> check {
      status should be (StatusCodes.BadRequest)
    }
  }

  "POST /123 with password but with invalid code" should "result in an error" in {
    // given
    val user = newRandomStoredUser()
    val code = PasswordResetCode(randomString(), user)
    passwordResetCodeDao.add(code).futureValue

    val newPassword = randomString()

    // when
    Post("/passwordreset/123", Map("password" -> newPassword)) ~> routes ~> check {
      status should be (StatusCodes.Forbidden)
      User.passwordsMatch(newPassword, userDao.findById(user.id).futureValue.get) should be (false)
    }
  }
}
