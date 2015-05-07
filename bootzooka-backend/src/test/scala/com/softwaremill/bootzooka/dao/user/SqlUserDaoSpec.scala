package com.softwaremill.bootzooka.dao.user

import java.util.UUID

import com.softwaremill.bootzooka.domain.User
import com.softwaremill.bootzooka.test.{ClearSqlDataAfterEach, FlatSpecWithSql}
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.BeforeAndAfterEach

import scala.language.implicitConversions

class SqlUserDaoSpec extends FlatSpecWithSql with BeforeAndAfterEach with ClearSqlDataAfterEach with LazyLogging {
  behavior of "SqlUserDao"

  val userDao = new SqlUserDao(sqlDatabase)

  def generateRandomId = UUID.randomUUID()

  lazy val randomIds: List[UUID] = List.fill(3)(generateRandomId)

  override def beforeEach() {
    super.beforeEach()

    for (i <- 1 to randomIds.size) {
      val login = "user" + i
      val password = "pass" + i
      val salt = "salt" + i
      val token = "token" + i
      userDao.add(User(randomIds(i - 1), login, login.toLowerCase, i + "email@sml.com", password, salt, token))
    }
  }

  it should "load all users" in {
    userDao.loadAll should have size 3
  }

  it should "count all users" in {
    userDao.countItems() should be (3)
  }

  it should "add new user" in {
    // Given
    val numberOfUsersBefore = userDao.countItems()
    val login = "newuser"
    val email = "newemail@sml.com"

    // When
    userDao.add(User(login, email, "pass", "salt", "token"))

    // Then
    (userDao.countItems() - numberOfUsersBefore) should be (1)
  }


  it should "throw exception when trying to add user with existing login" in {
    // Given
    val login = "newuser"
    val email = "anotherEmaill@sml.com"

    userDao.add(User(login, "somePrefix" + email, "somePass", "someSalt", "someToken"))

    // When & then
    intercept[Exception] {
      userDao.add(User(login, email, "pass", "salt", "token"))
    }
  }

  it should "throw exception when trying to add user with existing email" in {
    // Given
    val login = "anotherUser"
    val email = "newemail@sml.com"

    userDao.add(User("somePrefixed" + login, email, "somePass", "someSalt", "someToken"))

    // When
    intercept[Exception] {
      userDao.add(User(login, email, "pass", "salt", "token"))
    }
  }

  it should "remove user" in {
    // Given
    val numberOfUsersBefore = userDao.countItems()
    val userOpt: Option[User] = userDao.findByLoginOrEmail("user1")

    // When
    userOpt.foreach(u => userDao.remove(u.id))

    // Then
    userOpt should not be None
    (userDao.countItems() - numberOfUsersBefore) should be (-1)
  }

  it should "find by email" in {
    // Given
    val email: String = "1email@sml.com"

    // When
    val userOpt: Option[User] = userDao.findByEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be (email)
      case _ => fail("User option should be defined")
    }
  }

  it should "find by uppercased email" in {
    // Given
    val email: String = "1email@sml.com".toUpperCase

    // When
    val userOpt: Option[User] = userDao.findByEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be (email.toLowerCase)
      case _ => fail("User option should be defined")
    }
  }

  it should "find by login" in {
    // Given
    val login: String = "user1"

    // When
    val userOpt: Option[User] = userDao.findByLowerCasedLogin(login)

    // Then
    userOpt match {
      case Some(u) => u.login should be (login)
      case _ => fail("User option should be defined")
    }
  }

  it should "find users by identifiers" in {
    // Given
    val ids = Set(randomIds(0), randomIds(1), randomIds(1))

    // When
    val users = userDao.findForIdentifiers(ids)

    // Then
    users.map(user => user.login) should contain theSameElementsAs List("user1", "user2")
  }

  it should "find by uppercased login" in {
    // Given
    val login: String = "user1".toUpperCase

    // When
    val userOpt: Option[User] = userDao.findByLowerCasedLogin(login)

    // Then
    userOpt match {
      case Some(u) => u.login should be (login.toLowerCase)
      case _ => fail("User option should be defined")
    }
  }

  it should "find using login with findByLoginOrEmail" in {
    // Given
    val login: String = "user1"

    // When
    val userOpt: Option[User] = userDao.findByLoginOrEmail(login)

    // Then
    userOpt match {
      case Some(u) => u.login should be (login.toLowerCase)
      case _ => fail("User option should be defined")
    }
  }

  it should "find using uppercased login with findByLoginOrEmail" in {
    // Given
    val login: String = "user1".toUpperCase

    // When
    val userOpt: Option[User] = userDao.findByLoginOrEmail(login)

    // Then
    userOpt match {
      case Some(u) => u.login should be (login.toLowerCase)
      case _ => fail("User option should be defined")
    }
  }

  it should "find using email with findByLoginOrEmail" in {
    // Given
    val email: String = "1email@sml.com"

    // When
    val userOpt: Option[User] = userDao.findByLoginOrEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be (email.toLowerCase)
      case _ => fail("User option should be defined")
    }
  }

  it should "find using uppercased email with findByLoginOrEmail" in {
    // Given
    val email: String = "1email@sml.com".toUpperCase

    // When
    val userOpt: Option[User] = userDao.findByLoginOrEmail(email)

    // Then
    userOpt match {
      case Some(u) => u.email should be (email.toLowerCase)
      case _ => fail("User option should be defined")
    }
  }

  it should "find by token" in {
    // Given
    val token = "token1"

    // When
    val userOpt: Option[User] = userDao.findByToken(token)

    // Then
    userOpt match {
      case Some(u) => u.token should be (token)
      case _ => fail("User option should be defined")
    }
  }

  it should "change password" in {
    val login = "user1"
    val password = User.encryptPassword("pass11", "salt1")
    val user = userDao.findByLoginOrEmail(login).get
    userDao.changePassword(user.id, password)
    val postModifyUserOpt = userDao.findByLoginOrEmail(login)
    val u = postModifyUserOpt.get
    u should be (user.copy(password = password))
  }

  it should "change login" in {
    val user = userDao.findByLowerCasedLogin("user1")
    val u = user.get
    val newLogin: String = "changedUser1"
    userDao.changeLogin(u.login, newLogin)
    val postModifyUser = userDao.findByLowerCasedLogin(newLogin)
    postModifyUser match {
      case Some(pmu) => pmu should be (u.copy(login = newLogin, loginLowerCased = newLogin.toLowerCase))
      case None => fail("Changed user was not found. Maybe login wasn't really changed?")
    }
  }

  it should "change email" in {
    val newEmail = "newmail@sml.pl"
    val user = userDao.findByEmail("1email@sml.com")
    val u = user.get
    userDao.changeEmail(u.email, newEmail)
    userDao.findByEmail(newEmail) match {
      case Some(cu) => cu should be (u.copy(email = newEmail))
      case None => fail("User couldn't be found. Maybe e-mail wasn't really changed?")
    }
  }
}
