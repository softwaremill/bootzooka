package com.softwaremill.bootzooka.user.application

import java.util.UUID

import com.softwaremill.bootzooka.test.{FlatSpecWithDb, TestHelpersWithDb}
import com.softwaremill.bootzooka.user.domain.User
import org.scalatest.Matchers

class UserServiceSpec extends FlatSpecWithDb with Matchers with TestHelpersWithDb {

  override protected def beforeEach() = {
    super.beforeEach()

    userDao.add(newUser("Admin", "admin@sml.com", "pass", "salt")).futureValue
    userDao.add(newUser("Admin2", "admin2@sml.com", "pass", "salt")).futureValue
  }

  "registerNewUser" should "add user with unique lowercase login info" in {
    // When
    val result = userService.registerNewUser("John", "newUser@sml.com", "password").futureValue

    // Then
    result should be (UserRegisterResult.Success)

    val userOpt = userDao.findByLowerCasedLogin("John").futureValue
    userOpt should be ('defined)
    val user = userOpt.get

    user.login should be ("John")
    user.loginLowerCased should be ("john")

    emailService.wasEmailSentTo("newUser@sml.com") should be (true)
  }

  "registerNewUser" should "not register a user if a user with the given login/e-mail exists" in {
    // when
    val resultInitial = userService.registerNewUser("John", "newUser@sml.com", "password").futureValue
    val resultSameLogin = userService.registerNewUser("John", "newUser2@sml.com", "password").futureValue
    val resultSameEmail = userService.registerNewUser("John2", "newUser@sml.com", "password").futureValue

    // then
    resultInitial should be (UserRegisterResult.Success)
    resultSameLogin should matchPattern { case UserRegisterResult.UserExists(_) => }
    resultSameEmail should matchPattern { case UserRegisterResult.UserExists(_) => }

    userDao.findByLoginOrEmail("newUser2@sml.com").futureValue should be (None)
    userDao.findByLoginOrEmail("John2").futureValue should be (None)
  }

  "registerNewUser" should "not schedule an email on existing login" in {
    // When
    userService.registerNewUser("Admin", "secondEmail@sml.com", "password").futureValue

    // Then
    emailService.wasEmailSentTo("secondEmail@sml.com") should be (false)
  }

  "changeEmail" should "change email for specified user" in {
    val user = userDao.findByLowerCasedLogin("admin").futureValue
    val newEmail = "new@email.com"
    userService.changeEmail(user.get.id, newEmail).futureValue should be ('right)
    userDao.findByEmail(newEmail).futureValue match {
      case Some(cu) => // ok
      case None => fail("User not found. Maybe e-mail wasn't really changed?")
    }
  }

  "changeEmail" should "not change email if already used by someone else" in {
    userService.changeEmail(UUID.randomUUID(), "admin2@sml.com").futureValue should be ('left)
  }

  "changeLogin" should "change login for specified user" in {
    val user = userDao.findByLowerCasedLogin("admin").futureValue
    val newLogin = "newadmin"
    userService.changeLogin(user.get.id, newLogin).futureValue should be ('right)
    userDao.findByLowerCasedLogin(newLogin).futureValue match {
      case Some(cu) =>
      case None => fail("User not found. Maybe login wasn't really changed?")
    }
  }

  "changeLogin" should "not change login if already used by someone else" in {
    userService.changeLogin(UUID.randomUUID(), "admin2").futureValue should be ('left)
  }

  "changePassword" should "change password if current is correct and new is present" in {
    // Given
    val user = userDao.findByLowerCasedLogin("admin").futureValue.get
    val currentPassword = "pass"
    val newPassword = "newPass"

    // When
    val changePassResult = userService.changePassword(user.id, currentPassword, newPassword).futureValue

    // Then
    changePassResult should be ('right)
    userDao.findByLowerCasedLogin("admin").futureValue match {
      case Some(cu) => cu.password should be (User.encryptPassword(newPassword, cu.salt))
      case None => fail("Something bad happened, maybe mocked Dao is broken?")
    }
  }

  "changePassword" should "not change password if current is incorrect" in {
    // Given
    val user = userDao.findByLowerCasedLogin("admin").futureValue.get

    // When, Then
    userService.changePassword(user.id, "someillegalpass", "newpass").futureValue should be ('left)
  }

  "changePassword" should "complain when user cannot be found" in {
    userService.changePassword(UUID.randomUUID(), "pass", "newpass").futureValue should be ('left)
  }

}
