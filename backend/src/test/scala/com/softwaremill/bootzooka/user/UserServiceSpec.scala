package com.softwaremill.bootzooka.user

import java.util.UUID

import com.softwaremill.bootzooka.email.{EmailContentWithSubject, EmailService, EmailTemplatingEngine}
import com.softwaremill.bootzooka.test.{FlatSpecWithSql, UserTestHelpers}
import org.mockito.BDDMockito._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest
import org.scalatest.mock.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserServiceSpec extends FlatSpecWithSql with scalatest.Matchers with MockitoSugar with UserTestHelpers {
  val emailService = mock[EmailService]
  val emailTemplatingEngine = mock[EmailTemplatingEngine]

  val userDao = new UserDao(sqlDatabase)
  val userService = new UserService(userDao, emailService, emailTemplatingEngine)

  override protected def beforeEach() = {
    super.beforeEach()

    userDao.add(newUser("Admin", "admin@sml.com", "pass", "salt")).futureValue
    userDao.add(newUser("Admin2", "admin2@sml.com", "pass", "salt")).futureValue
  }

  "checkExistence" should "don't find given user login and e-mail" in {
    val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("newUser", "newUser@sml.com")
      .futureValue
    userExistence.isRight should be (true)
  }

  "checkExistence" should "find duplicated login" in {
    val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("Admin", "newUser@sml.com")
      .futureValue

    userExistence.isLeft should be (true)
    userExistence.left.get.equals("Login already in use!")
  }

  "checkExistence" should "find duplicated login written as upper cased string" in {
    val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("ADMIN", "newUser@sml.com")
      .futureValue

    userExistence.isLeft should be (true)
    userExistence.left.get.equals("Login already in use!")
  }

  "checkExistence" should "find duplicated email" in {
    val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("newUser", "admin@sml.com")
      .futureValue

    userExistence.isLeft should be (true)
    userExistence.left.get.equals("E-mail already in use!")
  }

  "checkExistence" should "find duplicated email written as upper cased string" in {
    val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("newUser", "ADMIN@sml.com")
      .futureValue

    userExistence.isLeft should be (true)
    userExistence.left.get.equals("E-mail already in use!")
  }

  "registerNewUser" should "add user with unique lowercased login info" in {
    // Given
    given(emailService.scheduleEmail(any(), any())).willReturn(Future {})

    // When
    val result = userService.registerNewUser("John", "newUser@sml.com", "password").futureValue

    // Then
    result should be (UserRegisterResult.Success)

    val userOpt = userDao.findByLowerCasedLogin("John").futureValue
    userOpt.isDefined should be (true)
    val user = userOpt.get

    user.login should be ("John")
    user.loginLowerCased should be ("john")
    verify(emailTemplatingEngine).registrationConfirmation(Matchers.eq("John"))
    verify(emailService)
      .scheduleEmail(Matchers.eq("newUser@sml.com"), any[EmailContentWithSubject])
  }

  "registerNewUser" should "not schedule an email on existing login" in {
    // When
    try {
      userService.registerNewUser("Admin", "secondEmail@sml.com", "password").futureValue
    }
    catch {
      case e: Exception =>
    }
    // Then
    verify(emailService, never()).scheduleEmail(Matchers.eq("secondEmail@sml.com"), any[EmailContentWithSubject])
  }

  "changeEmail" should "change email for specified user" in {
    val user = userDao.findByLowerCasedLogin("admin").futureValue
    val newEmail = "new@email.com"
    userService.changeEmail(user.get.id, newEmail).futureValue should be ('right)
    userDao.findByEmail(newEmail).futureValue match {
      case Some(cu) =>
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
