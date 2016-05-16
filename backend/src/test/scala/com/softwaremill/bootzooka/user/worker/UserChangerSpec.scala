package com.softwaremill.bootzooka.user.worker

import java.util.UUID

import com.softwaremill.bootzooka.test.{BaseActorPerRequestSpec, TestHelpersWithDb}
import com.softwaremill.bootzooka.user._
import com.softwaremill.bootzooka.user.worker.UserChanger._
import org.scalatest.Matchers

class UserChangerSpec extends BaseActorPerRequestSpec with Matchers with TestHelpersWithDb {

  override protected def beforeEach() = {
    super.beforeEach()

    userDao.add(newUser("Admin", "admin@sml.com", "pass", "salt")).futureValue
    userDao.add(newUser("Admin2", "admin2@sml.com", "pass", "salt")).futureValue
  }

  "changeEmail" should "change email for specified user" in {
    val user = userDao.findByLowerCasedLogin("admin").futureValue
    val newEmail = "new@email.com"

    userChanger whenSend ChangeEmail(user.get.id, newEmail) thenExpect EmailChanged

    userDao.findByEmail(newEmail).futureValue match {
      case Some(cu) => // ok
      case None => fail("User not found. Maybe e-mail wasn't really changed?")
    }
  }

  "changeEmail" should "not change email if already used by someone else" in {
    userChanger whenSend ChangeEmail(UUID.randomUUID(), "admin2@sml.com") thenExpect EmailIsTaken
  }

  "changeLogin" should "change login for specified user" in {
    val user = userDao.findByLowerCasedLogin("admin").futureValue
    val newLogin = "newadmin"

    userChanger whenSend ChangeLogin(user.get.id, newLogin) thenExpect LoginChanged

    userDao.findByLowerCasedLogin(newLogin).futureValue match {
      case Some(cu) =>
      case None => fail("User not found. Maybe login wasn't really changed?")
    }
  }

  "changeLogin" should "not change login if already used by someone else" in {
    userChanger whenSend ChangeLogin(UUID.randomUUID(), "admin2") thenExpect LoginIsTaken
  }

  "changePassword" should "change password if current is correct and new is present" in {
    // Given
    val user = userDao.findByLowerCasedLogin("admin").futureValue.get
    val currentPassword = "pass"
    val newPassword = "newPass"

    // When
    userChanger whenSend ChangePassword(user.id, currentPassword, newPassword) thenExpect PasswordChanged

    userDao.findByLowerCasedLogin("admin").futureValue match {
      case Some(cu) => cu.password should be (User.encryptPassword(newPassword, cu.salt))
      case None => fail("Something bad happened, maybe mocked Dao is broken?")
    }
  }

  "changePassword" should "not change password if current is incorrect" in {
    // Given
    val user = userDao.findByLowerCasedLogin("admin").futureValue.get

    // When, Then
    userChanger whenSend ChangePassword(user.id, "someillegalpass", "newpass") thenExpect PasswordIsInvalid
  }

  "changePassword" should "complain when user cannot be found" in {
    userChanger whenSend ChangePassword(UUID.randomUUID(), "pass", "newpass") thenExpect UserIdIsInvalid
  }
}
