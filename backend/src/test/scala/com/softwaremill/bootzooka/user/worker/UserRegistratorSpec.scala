package com.softwaremill.bootzooka.user.worker

import com.softwaremill.bootzooka.test.{BaseActorPerRequestSpec, TestHelpersWithDb}
import com.softwaremill.bootzooka.user.worker.UserRegistrator._
import org.scalatest.Matchers

class UserRegistratorSpec extends BaseActorPerRequestSpec with Matchers with TestHelpersWithDb {

  override protected def beforeEach() = {
    super.beforeEach()

    userDao.add(newUser("Admin", "admin@sml.com", "pass", "salt")).futureValue
    userDao.add(newUser("Admin2", "admin2@sml.com", "pass", "salt")).futureValue
  }

  "registerNewUser" should "add user with unique lowercase login info" in {

    userRegistrator whenSend RegisterUser("John", "newUser@sml.com", "password") thenExpect UserRegistered

    val userOpt = userDao.findByLowerCasedLogin("John").futureValue
    userOpt should be('defined)
    val user = userOpt.get

    user.login should be("John")
    user.loginLowerCased should be("john")

    emailService.wasEmailSentTo("newUser@sml.com") should be(true)
  }

  "registerNewUser" should "not register a user if a user with the given login/e-mail exists" in {

    userRegistrator whenSend RegisterUser("John", "newUser@sml.com", "password") thenExpect UserRegistered

    userRegistrator whenSend RegisterUser("John", "newUser2@sml.com", "password") thenExpect LoginIsTaken

    userRegistrator whenSend RegisterUser("John2", "newUser@sml.com", "password") thenExpect EmailIsTaken

    userDao.findByLoginOrEmail("newUser2@sml.com").futureValue should be(None)
    userDao.findByLoginOrEmail("John2").futureValue should be(None)
  }

  "registerNewUser" should "not schedule an email on existing login" in {
    userRegistrator whenSend RegisterUser("John", "newUser@sml.com", "password") thenExpect UserRegistered

    // Then
    emailService.wasEmailSentTo("secondEmail@sml.com") should be(false)
  }
}
