package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.common.Utils
import com.weiglewilczek.slf4s.Logging

class MongoUserDAOSpec extends SpecificationWithMongo with Logging {

  var userDAO: UserDAO = null

  "MongoUserDAO" should {

    step({
      userDAO = new MongoUserDAO()

      for(i <- 1 to 3) {
        val login = "user" + i
        val password: String = "pass" + 1
        userDAO.add(User(login, i +"email@sml.com", Utils.sha256(password, login.toLowerCase),
          Utils.sha256(password, login.toLowerCase)))
      }
    })

    "load all users" in {
      assert(userDAO.loadAll.size === 3)
    }

    "count all users" in {
      assert(userDAO.countItems() === 3)
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
      assert(userDAO.countItems() - numberOfUsersBefore === 1)
    }


    "throw exception when trying to add user with existing login" in {
      // Given
      val login = "newuser"
      val email = "anotherEmaill@sml.com"

      // When
      userDAO.add(User(login, email, Utils.sha256("pass", login.toLowerCase),
        Utils.sha256("pass", login.toLowerCase))) should(throwA[Exception])(message = "User with given e-mail or login already exists")
    }

    "throw exception when trying to add user with existing email" in {
      // Given
      val login = "anotherUser"
      val email = "newemail@sml.com"

      // When
      userDAO.add(User(login, email, Utils.sha256("pass", login.toLowerCase),
        Utils.sha256("pass", login.toLowerCase))) should(throwA[Exception])(message = "User with given e-mail or login already exists")
    }

    "remove user" in {
      // Given
      val numberOfUsersBefore = userDAO.countItems()
      val userOpt: Option[User] = userDAO.findByLoginOrEmail("newuser")

      // When
      userOpt.foreach(u => userDAO.remove(u._id.toString))

      // Then
      assert(userDAO.countItems() - numberOfUsersBefore === -1)
    }

    "find by email" in {
      // Given
      val email: String = "1email@sml.com"

      // When
      val userOpt: Option[User] = userDAO.findByEmail(email)

      // Then
      userOpt match {
        case Some(u) => assert(u.email.equals(email) === true)
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
        case Some(u) => assert(u.email.equalsIgnoreCase(email) === true)
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
        case Some(u) => assert(u.login.equals(login) === true)
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
        case Some(u) => assert(u.login.equalsIgnoreCase(login) === true)
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
        case Some(u) => assert(u.login.equalsIgnoreCase(login) === true)
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
        case Some(u) => assert(u.login.equalsIgnoreCase(login) === true)
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
        case Some(u) => assert(u.email.equalsIgnoreCase(email) === true)
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
        case Some(u) => assert(u.email.equalsIgnoreCase(email) === true)
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
        case Some(u) => assert(u.token.equals(token) === true)
        case _ => failure("User option should be defined")
      }
    }

    "not find by uppercased token" in {
      // Given
      val token = Utils.sha256("pass1", "user1").toUpperCase

      // When
      val userOpt: Option[User] = userDAO.findByToken(token)

      // Then
      assert(userOpt.isEmpty === true)
    }

    "should find by login and password" in {
      // Given
      val login: String = "user1"

      // When
      val userOpt: Option[User] = userDAO.findByLoginAndEncryptedPassword(login, Utils.sha256("pass1", login))
      val users = userDAO.loadAll

      // Then
      userOpt match {
        case Some(u) => assert(u.login.equalsIgnoreCase(login))
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
        case Some(u) => assert(u.login.equalsIgnoreCase(login))
        case _ => failure("User option should be defined")
      }
    }

    "should not find by login and invalid password" in {
      // Given
      val login: String = "user1"

      // When
      val userOpt: Option[User] = userDAO.findByLoginAndEncryptedPassword(login, Utils.sha256("invalid", login))

      // Then
      assert(userOpt.isEmpty === true)
    }

    "change password" in {
        val login = "user1"
        val password = Utils.sha256("newPassword", login)
        val preModifyUserOpt = userDAO.findByLoginOrEmail(login)
        userDAO.changePassword(preModifyUserOpt.get._id.toString, password)
        val postModifyUserOpt = userDAO.findByLoginOrEmail(login)
        postModifyUserOpt foreach(user => assert(user.password === password))
    }

  }

}
