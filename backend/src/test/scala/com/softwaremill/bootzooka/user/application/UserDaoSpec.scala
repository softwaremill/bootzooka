package com.softwaremill.bootzooka.user.application

import java.util.UUID

import com.softwaremill.bootzooka.test.{FlatSpecWithDb, TestHelpers}
import com.softwaremill.bootzooka.user.domain.User
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.Matchers

import scala.language.implicitConversions

class UserDaoSpec extends FlatSpecWithDb with StrictLogging with TestHelpers with Matchers {
  behavior of "UserDao"

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val userDao = new UserDao(sqlDatabase)
  lazy val randomIds = List.fill(3)(UUID.randomUUID())

  override def beforeEach() {
    super.beforeEach()
    for (i <- 1 to randomIds.size) {
      val login = "user" + i
      val password = "pass" + i
      val salt = "salt" + i
      userDao.add(User(randomIds(i - 1), login, login.toLowerCase, i + "email@sml.com", password, salt,
        createdOn))
        .futureValue
    }
  }

  it should "add new user" in {
    // Given
    val login = "newuser"
    val email = "newemail@sml.com"

    // When
    userDao.add(newUser(login, email, "pass", "salt")).futureValue

    // Then
    userDao.findByEmail(email).futureValue should be ('defined)
  }

  it should "fail with exception when trying to add user with existing login" in {
    // Given
    val login = "newuser"
    val email = "anotherEmaill@sml.com"

    userDao.add(newUser(login, "somePrefix" + email, "somePass", "someSalt")).futureValue

    // When & then
    userDao.add(newUser(login, email, "pass", "salt")).failed.futureValue
  }

  it should "fail with exception when trying to add user with existing email" in {
    // Given
    val login = "anotherUser"
    val email = "newemail@sml.com"

    userDao.add(newUser("somePrefixed" + login, email, "somePass", "someSalt")).futureValue

    // When & then
    userDao.add(newUser(login, email, "pass", "salt")).failed.futureValue
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
    userDao.changeLogin(u.id, newLogin).futureValue
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
    userDao.changeEmail(u.id, newEmail).futureValue

    // Then
    userDao.findByEmail(newEmail).futureValue should equal(Some(u.copy(email = newEmail)))
  }

}
