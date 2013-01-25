package pl.softwaremill.bootstrap.service.user

import org.specs2.mutable.Specification
import pl.softwaremill.bootstrap.dao.{InMemoryUserDAO, UserDAO}
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.domain.User
import pl.softwaremill.bootstrap.service.schedulers.EmailSendingService
import pl.softwaremill.bootstrap.service.templates.{EmailContentWithSubject, EmailTemplatingEngine}
import org.mockito.Matchers
import pl.softwaremill.bootstrap.service.data.UserJson

class UserServiceSpec extends Specification with Mockito {

  def prepareUserDAOMock: UserDAO = {
    val dao = new InMemoryUserDAO
    dao.add(User("Admin", "admin@sml.com", "pass", "salt"))
    dao.add(User("Admin2", "admin2@sml.com", "pass", "salt"))
    dao
  }

  val registrationDataValidator: RegistrationDataValidator = mock[RegistrationDataValidator]
  val emailSendingService: EmailSendingService = mock[EmailSendingService]
  val emailTemplatingEngine = mock[EmailTemplatingEngine]
  val userService = new UserService(prepareUserDAOMock, registrationDataValidator, emailSendingService, emailTemplatingEngine)

  "findByEmail" should {
    // this test is silly :\
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
    val userService = new UserService(prepareUserDAOMock, registrationDataValidator, emailSendingService, emailTemplatingEngine)

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
    val userService = new UserService(userDAOMock, registrationDataValidator, emailSendingService, emailTemplatingEngine)

    "add user with duplicated lowercased login info" in {
      // When
      userService.registerNewUser("John", "newUser@sml.com", "password")

      // Then
      val userOpt: Option[User] = userDAOMock.findByLowerCasedLogin("John")
      userOpt.isDefined === true
      val user = userOpt.get

      there was user.login === "John"
      there was user.loginLowerCased === "john"
      there was one(emailTemplatingEngine).registrationConfirmation(Matchers.eq("John"))
      there was one(emailSendingService)
        .scheduleEmail(Matchers.eq("newUser@sml.com"), any[EmailContentWithSubject])
    }

    "not schedule an email on existing login" in {
      // When
      try {
        userService.registerNewUser("John", "secondEmail@sml.com", "password")
      }
      catch {
        case e: Exception =>
      }
      // Then
      there was no(emailSendingService)
        .scheduleEmail(Matchers.eq("secondEmail@sml.com"), any[EmailContentWithSubject])
    }

  }

  "changeEmail" should {
    val userDAO: UserDAO = prepareUserDAOMock
    val userService = new UserService(userDAO, registrationDataValidator, emailSendingService, emailTemplatingEngine)

    "change email for specified user" in {
      val user = userDAO.findByLowerCasedLogin("admin")
      val userEmail = user.get.email
      val newEmail = "new@email.com"
      userService.changeEmail(userEmail, newEmail) must beRight[Unit]
      userDAO.findByEmail(newEmail) match {
        case Some(cu) => success
        case None => failure("User not found. Maybe e-mail wasn't really changed?")
      }
    }

    "not change email if already used by someone else" in {
       userService.changeEmail("admin@sml.com", "admin2@sml.com") must beLeft[String]("E-mail used by another user")
    }
  }

  "changeLogin" should {
    val userDAO: UserDAO = prepareUserDAOMock
    val userService = new UserService(userDAO, registrationDataValidator, emailSendingService, emailTemplatingEngine)

    "change login for specified user" in {
      val user = userDAO.findByLowerCasedLogin("admin")
      val userLogin = user.get.login
      val newLogin = "newadmin"
      userService.changeLogin(userLogin, newLogin) must beRight[Unit]
      userDAO.findByLowerCasedLogin(newLogin) match {
        case Some(cu) => success
        case None => failure("User not found. Maybe login wasn't really changed?")
      }
    }

    "not change login if already used by someone else" in {
      userService.changeLogin("admin", "admin2") must beLeft[String]("Login is already taken")
    }
  }

}
