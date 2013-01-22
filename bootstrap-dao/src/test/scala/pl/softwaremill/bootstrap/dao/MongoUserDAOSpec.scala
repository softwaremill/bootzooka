package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.common.Utils
import com.weiglewilczek.slf4s.Logging

class MongoUserDAOSpec extends SpecificationWithMongo with Logging {

  var userDAO: UserDAO = null

  "MongoUserDAO" should {

    step({
      userDAO = new MongoUserDAO()

      for (i <- 1 to 3) {
        val login = "user" + i
        val password: String = "pass" + 1
        userDAO.add(User(login, i + "email@sml.com", Utils.sha256(password, login.toLowerCase),
          Utils.sha256(password, login.toLowerCase)))
      }
    })

    "load all users" in {
      userDAO.loadAll must have size (3)
    }

    "count all users" in {
      userDAO.countItems() must be equalTo (3)
    }

    "add new user" in {
      // Given
      val numberOfUsersBefore = userDAO.countItems()
      val login = "newuser"
      val email = "newemail@sml.com"

      // When
      userDAO.add(User(login, email, Utils.sha256("pass", login.toLowerCase),
        Utils.sha256("pass", login.toLowerCase)))

      // Then
      userDAO.countItems() - numberOfUsersBefore must be equalTo (1)
    }


    "throw exception when trying to add user with existing login" in {
      // Given
      val login = "newuser"
      val email = "anotherEmaill@sml.com"

      // When
      userDAO.add(User(login, email, Utils.sha256("pass", login.toLowerCase),
        Utils.sha256("pass", login.toLowerCase))) should (throwA[Exception])(message = "User with given e-mail or login already exists")
    }

    "throw exception when trying to add user with existing email" in {
      // Given
      val login = "anotherUser"
      val email = "newemail@sml.com"

      // When
      userDAO.add(User(login, email, Utils.sha256("pass", login.toLowerCase),
        Utils.sha256("pass", login.toLowerCase))) should (throwA[Exception])(message = "User with given e-mail or login already exists")
    }

    "remove user" in {
      // Given
      val numberOfUsersBefore = userDAO.countItems()
      val userOpt: Option[User] = userDAO.findByLoginOrEmail("newuser")

      // When
      userOpt.foreach(u => userDAO.remove(u._id.toString))

      // Then
      (userDAO.countItems() - numberOfUsersBefore) must be equalTo (-1)
    }

    "find by email" in {
      // Given
      val email: String = "1email@sml.com"

      // When
      val userOpt: Option[User] = userDAO.findByEmail(email)

      // Then
      userOpt match {
        case Some(u) => u.email must be equalTo (email)
        case _ => failure("User option should be defined")
      }
    }

    "find by uppercased email" in {
      // Given
      val email: String = "1email@sml.com".toUpperCase

      // When
      val userOpt: Option[User] = userDAO.findByEmail(email)

      // Then
      userOpt match {
        case Some(u) => u.email must beEqualTo(email).ignoreCase
        case _ => failure("User option should be defined")
      }
    }

    "find by login" in {
      // Given
      val login: String = "user1"

      // When
      val userOpt: Option[User] = userDAO.findByLowerCasedLogin(login)

      // Then
      userOpt match {
        case Some(u) => u.login must be equalTo (login)
        case _ => failure("User option should be defined")
      }
    }

    "find by uppercased login" in {
      // Given
      val login: String = "user1".toUpperCase

      // When
      val userOpt: Option[User] = userDAO.findByLowerCasedLogin(login)

      // Then
      userOpt match {
        case Some(u) => u.login must beEqualTo(login).ignoreCase
        case _ => failure("User option should be defined")
      }
    }

    "find using login with findByLoginOrEmail" in {
      // Given
      val login: String = "user1"

      // When
      val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)

      // Then
      userOpt match {
        case Some(u) => u.login must beEqualTo(login).ignoreCase
        case _ => failure("User option should be defined")
      }
    }

    "find using uppercased login with findByLoginOrEmail" in {
      // Given
      val login: String = "user1".toUpperCase

      // When
      val userOpt: Option[User] = userDAO.findByLoginOrEmail(login)

      // Then
      userOpt match {
        case Some(u) => u.login must beEqualTo(login).ignoreCase
        case _ => failure("User option should be defined")
      }
    }

    "find using email with findByLoginOrEmail" in {
      // Given
      val email: String = "1email@sml.com"

      // When
      val userOpt: Option[User] = userDAO.findByLoginOrEmail(email)

      // Then
      userOpt match {
        case Some(u) => u.email must beEqualTo(email).ignoreCase
        case _ => failure("User option should be defined")
      }
    }

    "find using uppercased email with findByLoginOrEmail" in {
      // Given
      val email: String = "1email@sml.com".toUpperCase

      // When
      val userOpt: Option[User] = userDAO.findByLoginOrEmail(email)

      // Then
      userOpt match {
        case Some(u) => u.email must beEqualTo(email).ignoreCase
        case _ => failure("User option should be defined")
      }
    }

    "find by token" in {
      // Given
      val token = Utils.sha256("pass1", "user1")

      // When
      val userOpt: Option[User] = userDAO.findByToken(token)

      // Then
      userOpt match {
        case Some(u) => u.token must be equalTo (token)
        case _ => failure("User option should be defined")
      }
    }

    "not find by uppercased token" in {
      // Given
      val token = Utils.sha256("pass1", "user1").toUpperCase

      // When
      val userOpt: Option[User] = userDAO.findByToken(token)

      // Then
      userOpt must be none
    }

    "should find by login and password" in {
      // Given
      val login: String = "user1"

      // When
      val userOpt: Option[User] = userDAO.findByLoginAndEncryptedPassword(login, Utils.sha256("pass1", login))
      val users = userDAO.loadAll

      // Then
      userOpt match {
        case Some(u) => u.login must beEqualTo(login).ignoreCase
        case _ => failure("User option should be defined")
      }
    }

    "should find by uppercased login and password" in {
      // Given
      val login: String = "user1"

      // When
      val userOpt: Option[User] = userDAO.findByLoginAndEncryptedPassword(login.toUpperCase, Utils.sha256("pass1", login))

      // Then
      userOpt match {
        case Some(u) => u.login must beEqualTo(login).ignoreCase
        case _ => failure("User option should be defined")
      }
    }

    "should not find by login and invalid password" in {
      // Given
      val login: String = "user1"

      // When
      val userOpt: Option[User] = userDAO.findByLoginAndEncryptedPassword(login, Utils.sha256("invalid", login))

      // Then
      userOpt must be none
    }

    "change password" in {
      val login = "user1"
      val password = Utils.sha256("newPassword", login)
      val user = userDAO.findByLoginOrEmail(login).get
      userDAO.changePassword(user, password)
      val postModifyUserOpt = userDAO.findByLoginOrEmail(login)
      val u = postModifyUserOpt.get
      (u.password must be equalTo password) and
        (u.login must be equalTo user.login) and
        (u.email must be equalTo user.email) and
        (u._id must be equalTo user._id)
    }

    "change login" in {
      val user = userDAO.findByLowerCasedLogin("user1")
      val u = user.get
      val newLogin: String = "changedUser1"
      userDAO.changeLogin(u.login, newLogin)
      val postModifyUser = userDAO.findByLowerCasedLogin(newLogin)
      postModifyUser match {
        case Some(pmu) => {
          (pmu._id must be equalTo u._id) and
            (pmu.login must be equalTo newLogin) and
            (pmu.email must be equalTo u.email) and
            (pmu.password must be equalTo u.password) and
            (pmu.token must be equalTo u.token)
        }
        case None => failure("Changed user was not found. Maybe login wasn't really changed?")
      }
    }

    "change email" in {
      val newEmail = "newmail@sml.pl"
      val user = userDAO.findByEmail("1email@sml.com")
      val u = user.get
      userDAO.changeEmail(u.email, newEmail)
      userDAO.findByEmail(newEmail) match {
        case Some(cu) => {
          (cu._id must be equalTo u._id) and
            (cu.login must be equalTo u.login) and
            (cu.password must be equalTo u.password) and
            (cu.token must be equalTo u.token)
        }
        case None => failure("User couldn't be found. Maybe e-mail wasn't really changed?")
      }
    }
  }
}
