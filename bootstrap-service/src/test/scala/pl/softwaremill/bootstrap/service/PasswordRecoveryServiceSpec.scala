package pl.softwaremill.bootstrap.service

import org.specs2.mutable.Specification
import schedulers.EmailSendingService
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.dao.{PasswordResetCodeDAO, UserDAO, InMemoryUserDAO}
import pl.softwaremill.bootstrap.domain.{PasswordResetCode, User}
import org.specs2.specification.Fragment
import templates.{EmailTemplatingEngine, EmailContentWithSubject}
import org.mockito.Matchers
import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
 * .
 */
class PasswordRecoveryServiceSpec extends Specification with Mockito {
  val invalidLogin = "user2"
  val validLogin = "user"

  def withCleanMocks(test: (UserDAO, PasswordResetCodeDAO, EmailSendingService, PasswordRecoveryService, EmailTemplatingEngine) => Fragment) = {
    val userDao = prepareUserDaoMock
    val codeDao = mock[PasswordResetCodeDAO]
    val emailSendingService = mock[EmailSendingService]
    val emailTemplatingEngine = mock[EmailTemplatingEngine]
    val passwordRecoveryService = new PasswordRecoveryService(userDao, codeDao, emailSendingService, emailTemplatingEngine)

    test(userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine)
  }

  def prepareUserDaoMock = {
    val userDao = mock[InMemoryUserDAO]
    userDao.findByLoginOrEmail(validLogin) returns Some(User(validLogin, "user@sml.pl", "pass", "salt", "token"))
    userDao.findByLoginOrEmail(invalidLogin) returns None
    userDao
  }

  "sendResetCodeToUser" should {

    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      "search for user using provided login" in {
        passwordRecoveryService.sendResetCodeToUser(invalidLogin)
        there was one(userDao).findByLoginOrEmail(invalidLogin)
      }
    })

    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      "do nothing when login doesn't exist" in {
        passwordRecoveryService.sendResetCodeToUser(invalidLogin)
        there was no(emailSendingService).scheduleEmail(anyString, any)
      }
    })

    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      "store generated code for reuse" in {
        passwordRecoveryService.sendResetCodeToUser(validLogin)
        there was one(codeDao).store(any[PasswordResetCode])
      }
    })

    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      "send e-mail to user containing link to reset page with generated reset code" in {
        passwordRecoveryService.sendResetCodeToUser(validLogin)
        there was one(emailSendingService).scheduleEmail(Matchers.eq("user@sml.pl"), any[EmailContentWithSubject])
      }
    })

    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      "use template to generate e-mail" in {
        passwordRecoveryService.sendResetCodeToUser(validLogin)
        there was one(emailTemplatingEngine).passwordReset(Matchers.eq(validLogin), anyString)
      }
    })

    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      "change password for user" in {
        //Given
        val code = "validCode"
        val login = "login"
        val userId = "id"
        val password = "password"
        val salt = "salt"
        val mockUserId = mock[ObjectId]
        val mockCode = mock[PasswordResetCode]
        val mockUser = mock[User]

        mockUserId.toString returns userId

        mockCode.userId returns mockUserId
        mockCode.validTo returns new DateTime().plusHours(1)

        mockUser._id returns mockUserId
        mockUser.login returns login
        mockUser.salt returns "salt"

        codeDao.load(code) returns (Some(mockCode))
        userDao.load(userId) returns (Some(mockUser))

        //When
        val result = passwordRecoveryService.performPasswordReset(code, password)

        //Then
        assert(result.isRight)
        assert(result.right.get)
        there was one(codeDao).load(code)
        there was one(userDao).changePassword(Matchers.eq(userId), Matchers.eq(User.encryptPassword(password, salt)))
        there was one(codeDao).delete(mockCode)
      }
    })

    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      "not change password when code is past it's valid date" in {
        //Given
        val password = "password"
        val code = "validCode"
        val mockCode = mock[PasswordResetCode]
        mockCode.validTo returns new DateTime().minusDays(2)
        codeDao.load(code) returns Some(mockCode)

        //When
        val result = passwordRecoveryService.performPasswordReset(code, password)

        //Then
        assert(result.isLeft)
        there was one(codeDao).delete(mockCode)
        there was no(userDao).changePassword(anyString, anyString)
      }
    })
  }
}
