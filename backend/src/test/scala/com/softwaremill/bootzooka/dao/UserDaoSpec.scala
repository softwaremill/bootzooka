package com.softwaremill.bootzooka.dao

import java.util.UUID

import com.softwaremill.bootzooka.domain.User
import com.softwaremill.bootzooka.test.{FlatSpecWithSql, UserTestHelpers}
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

class UserDaoSpec extends FlatSpecWithSql with LazyLogging with UserTestHelpers with Matchers {
  behavior of "UserDao"

  var userDao: UserDao = new UserDao(sqlDatabase)
  def generateRandomId = UUID.randomUUID()

  lazy val randomIds: List[UUID] = List.fill(3)(generateRandomId)

  override def beforeEach() {
    super.beforeEach()
    for (i <- 1 to randomIds.size) {
      val login = "user" + i
      val password = "pass" + i
      val salt = "salt" + i
      val token = "token" + i
      userDao.add(User(randomIds(i - 1), login, login.toLowerCase, i + "email@sml.com", password, salt, token,
        createdOn))
        .futureValue
    }
  }

  it should "add new user" in {
    // Given
    val login = "newuser"
    val email = "newemail@sml.com"

    // When
    userDao.add(newUser(login, email, "pass", "salt", "token")).futureValue

    // Then
    userDao.findByEmail(email).futureValue should be ('defined)
  }

  it should "fail with exception when trying to add user with existing login" in {
    // Given
    val login = "newuser"
    val email = "anotherEmaill@sml.com"

    userDao.add(newUser(login, "somePrefix" + email, "somePass", "someSalt", "someToken")).futureValue

    // When & then
    userDao.add(newUser(login, email, "pass", "salt", "token")).failed.futureValue.getMessage should equal(
      "User with given e-mail or login already exists"
    )
  }

  it should "fail with exception when trying to add user with existing email" in {
    // Given
    val login = "anotherUser"
    val email = "newemail@sml.com"

    userDao.add(newUser("somePrefixed" + login, email, "somePass", "someSalt", "someToken")).futureValue

    // When & then
    userDao.add(newUser(login, email, "pass", "salt", "token")).failed.futureValue.getMessage should equal(
      "User with given e-mail or login already exists"
    )
  }

  it should "find by email" in {
    // Given
    val email = "1email@sml.com"

    // When
    val userOpt = userDao.findByEmail(email).futureValue

    // Then
    userOpt.map(_.email) should equal(Some(email))
  }

  it should "find by uppercase email" in {
    // Given
    val email = "1email@sml.com".toUpperCase

    // When
    val userOpt = userDao.findByEmail(email).futureValue

    // Then
    userOpt.map(_.email) should equal(Some(email.toLowerCase))
  }

  it should "find by login" in {
    // Given
    val login = "user1"

    // When
    val userOpt = userDao.findByLowerCasedLogin(login).futureValue

    // Then
    userOpt.map(_.login) should equal(Some(login))
  }

  it should "find by uppercase login" in {
    // Given
    val login = "user1".toUpperCase

    // When
    val userOpt = userDao.findByLowerCasedLogin(login).futureValue

    // Then
    userOpt.map(_.login) should equal(Some(login.toLowerCase))
  }

  it should "find using login with findByLoginOrEmail" in {
    // Given
    val login = "user1"

    // When
    val userOpt = userDao.findByLoginOrEmail(login).futureValue

    // Then
    userOpt.map(_.login) should equal(Some(login.toLowerCase))
  }

  it should "find using uppercase login with findByLoginOrEmail" in {
    // Given
    val login = "user1".toUpperCase

    // When
    val userOpt = userDao.findByLoginOrEmail(login).futureValue

    // Then
    userOpt.map(_.login) should equal(Some(login.toLowerCase))
  }

  it should "find using email with findByLoginOrEmail" in {
    // Given
    val email = "1email@sml.com"

    // When
    val userOpt = userDao.findByLoginOrEmail(email).futureValue

    // Then
    userOpt.map(_.email) should equal(Some(email.toLowerCase))
  }

  it should "find using uppercase email with findByLoginOrEmail" in {
    // Given
    val email = "1email@sml.com".toUpperCase

    // When
    val userOpt = userDao.findByLoginOrEmail(email).futureValue

    // Then
    userOpt.map(_.email) should equal(Some(email.toLowerCase))
  }

  it should "find by token" in {
    // Given
    val token = "token1"

    // When
    val userOpt = userDao.findByToken(token).futureValue

    // Then
    userOpt.map(_.token) should equal(Some(token))
  }

  it should "change password" in {
    // Given
    val login = "user1"
    val password = User.encryptPassword("pass11", "salt1")
    val user = userDao.findByLoginOrEmail(login).futureValue.get

    // When
    userDao.changePassword(user.id, password).futureValue
    val postModifyUserOpt = userDao.findByLoginOrEmail(login).futureValue
    val u = postModifyUserOpt.get

    // Then
    u should be (user.copy(password = password))
  }

  it should "change login" in {
    // Given
    val user = userDao.findByLowerCasedLogin("user1")
    val u = user.futureValue.get
    val newLogin = "changedUser1"

    // When
    userDao.changeLogin(u.login, newLogin).futureValue
    val postModifyUser = userDao.findByLowerCasedLogin(newLogin).futureValue

    // Then
    postModifyUser should equal(Some(u.copy(login = newLogin, loginLowerCased = newLogin.toLowerCase)))
  }

  it should "change email" in {
    // Given
    val newEmail = "newmail@sml.pl"
    val user = userDao.findByEmail("1email@sml.com").futureValue
    val u = user.get

    // When
    userDao.changeEmail(u.email, newEmail).futureValue

    // Then
    userDao.findByEmail(newEmail).futureValue should equal(Some(u.copy(email = newEmail)))
  }

}
