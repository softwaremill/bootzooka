package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.domain.User
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfterAll
import org.bson.types.ObjectId
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.softwaremill.bootzooka.test.FlatSpecWithMongo

class MongoUserDAOSpec extends FlatSpecWithMongo with ShouldMatchers with BeforeAndAfterAll with LazyLogging {
  behavior of "MongoUserDAO"

  val userIdPrefix = "507f1f77bcf86cd79943901"
  val userDAO = new MongoUserDAO
  implicit def intSuffixToObjectId(suffix: Int): ObjectId = new ObjectId(userIdPrefix + suffix)

  override def beforeAll() {
    super.beforeAll()

    for (i <- 1 to 3) {
      val login = "user" + i
      val password = "pass" + i
      val salt = "salt" + i
      val token = "token" + i
      userDAO.add(User(i, login, login.toLowerCase, i + "email@sml.com", password, salt, token))
    }
  }

  it should "load all users" in {
    userDAO.loadAll should have size 3
  }

  it should "count all users" in {
    userDAO.countItems() should be (3)
  }

  it should "add new user" in {
    // Given
    val numberOfUsersBefore = userDAO.countItems()
    val login = "newuser"
    val email = "newemail@sml.com"

    // When
    userDAO.add(User(login, email, "pass", "salt", "token"))

    // Then
    (userDAO.countItems() - numberOfUsersBefore) should be (1)
  }


  it should "throw exception when trying to add user with existing login" in {
    // Given
    val login = "newuser"
    val email = "anotherEmaill@sml.com"

    // When & then
    intercept[Exception] {
      userDAO.add(User(login, email, "pass", "salt", "token"))
    }
  }

  it should "throw exception when trying to add user with existing email" in {
    // Given
    val login = "anotherUser"
    val email = "newemail@sml.com"

    // When
    intercept[Exception] {
      userDAO.add(User(login, email, "pass", "salt", "token"))
    }
  }

  it should "remove user" in {
    // Given
    val numberOfUsersBefore = userDAO.countItems()
    val userOpt: Option[User] = userDAO.findByLoginOrEmail("newuser")

    // When
    userOpt.foreach(u => userDAO.remove(u.id.toString))

    // Then
    (userDAO.countItems() - numberOfUsersBefore) should be (-1)
  }

  it should "find by email" in {
    // Given
    val email: String = "1email@sml.com"

    // When
    val userOpt: Option[User] = userDAO.findByEmail(email)

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
    val userOpt: Option[User] = userDAO.findByEmail(email)

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
    val userOpt: Option[User] = userDAO.findByLowerCasedLogin(login)

    // Then
    userOpt match {
      case Some(u) => u.login should be (login)
      case _ => fail("User option should be defined")
    }
  }

  it should "find users by identifiers" in {
    // Given
    val ids: List[ObjectId] = List(1, 2, 2)

    // When
    val users = userDAO.findForIdentifiers(ids)

    // Then
    users.map(user => user.login) should be(List("user1", "user2"))
  }

  it should "find by uppercased login" in {
    // Given
    val login: String = "user1".toUpperCase

    // When
    val userOpt: Option[User] = userDAO.findByLowerCasedLogin(login)

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
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)

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
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)

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
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(email)

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
    val userOpt: Option[User] = userDAO.findByLoginOrEmail(email)

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
    val userOpt: Option[User] = userDAO.findByToken(token)

    // Then
    userOpt match {
      case Some(u) => u.token should be (token)
      case _ => fail("User option should be defined")
    }
  }

  it should "change password" in {
    val login = "user1"
    val password = User.encryptPassword("pass11", "salt1")
    val user = userDAO.findByLoginOrEmail(login).get
    userDAO.changePassword(user.id.toString, password)
    val postModifyUserOpt = userDAO.findByLoginOrEmail(login)
    val u = postModifyUserOpt.get
    u should be (user.copy(password = password))
  }

  it should "change login" in {
    val user = userDAO.findByLowerCasedLogin("user1")
    val u = user.get
    val newLogin: String = "changedUser1"
    userDAO.changeLogin(u.login, newLogin)
    val postModifyUser = userDAO.findByLowerCasedLogin(newLogin)
    postModifyUser match {
      case Some(pmu) => pmu should be (u.copy(login = newLogin, loginLowerCased = newLogin.toLowerCase))
      case None => fail("Changed user was not found. Maybe login wasn't really changed?")
    }
  }

  it should "change email" in {
    val newEmail = "newmail@sml.pl"
    val user = userDAO.findByEmail("1email@sml.com")
    val u = user.get
    userDAO.changeEmail(u.email, newEmail)
    userDAO.findByEmail(newEmail) match {
      case Some(cu) => cu should be (u.copy(email = newEmail))
      case None => fail("User couldn't be found. Maybe e-mail wasn't really changed?")
    }
  }
}
