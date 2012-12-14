package pl.softwaremill.bootstrap.service.user

import org.specs2.mutable.Specification
import pl.softwaremill.bootstrap.dao.{InMemoryUserDAO, UserDAO}
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.service.data.UserJson

class UserServiceSpec extends Specification with Mockito {

  def prepareUserDAOMock: UserDAO = {
    val dao = new InMemoryUserDAO
    dao.add(User("Admin", "admin@sml.com", "pass"))
    dao.add(User("Admin2", "admin2@sml.com", "pass"))
    dao
  }

  val registrationDataValidator: RegistrationDataValidator = mock[RegistrationDataValidator]
  val userService = new UserService(prepareUserDAOMock, registrationDataValidator)

  "findByEmail" should { // this test is silly :\
    "return user for admin@sml.pl" in {
      val user: UserJson = userService.findByEmail("admin@sml.com").getOrElse(null)

      there was user !== null
      there was user.login === "Admin"
    }

    "return user for uppercased ADMIN@SML.PL" in {
      val user: UserJson = userService.findByEmail("ADMIN@SML.COM").getOrElse(null)

      there was user !== null
      there was user.login === "Admin"
    }
  }

  "checkExistence" should {
    val userService = new UserService(prepareUserDAOMock, registrationDataValidator)

    "don't find given user login and e-mail" in {
      val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("newUser", "newUser@sml.com")
      userExistence.isRight === true
    }

    "find duplicated login" in {
      val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("Admin", "newUser@sml.com")

      userExistence.isLeft === true
      userExistence.left.get.equals("Login already in use!")
    }

    "find duplicated login written as upper cased string" in {
      val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("ADMIN", "newUser@sml.com")

      userExistence.isLeft === true
      userExistence.left.get.equals("Login already in use!")
    }

    "find duplicated email" in {
      val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("newUser", "admin@sml.com")

      userExistence.isLeft === true
      userExistence.left.get.equals("E-mail already in use!")
    }

    "find duplicated email written as upper cased string" in {
      val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("newUser", "ADMIN@sml.com")

      userExistence.isLeft === true
      userExistence.left.get.equals("E-mail already in use!")
    }
  }

  "registerNewUser" should {
    val userDAOMock: UserDAO = prepareUserDAOMock
    val userService = new UserService(userDAOMock, registrationDataValidator)

    "add user with duplicated lowercased login info" in {
      // When
      userService.registerNewUser("John", "newUser@sml.com", "password")

      // Then
      val userOpt: Option[User] = userDAOMock.findByLowerCasedLogin("John")
      userOpt.isDefined === true
      val user = userOpt.get

      there was user.login === "John"
      there was user.loginLowerCased === "john"
    }
  }

}
