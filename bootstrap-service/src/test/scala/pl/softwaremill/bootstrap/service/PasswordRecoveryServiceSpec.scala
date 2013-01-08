package pl.softwaremill.bootstrap.service

import org.specs2.mutable.Specification
import schedulers.EmailSendingService
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.dao.{UserDAO, InMemoryUserDAO}
import pl.softwaremill.bootstrap.domain.User
import org.specs2.specification.Fragment
import templates.EmailContentWithSubject

/**
 * .
 */
class PasswordRecoveryServiceSpec extends Specification with Mockito {


  def withCleanMocks(test: (UserDAO, EmailSendingService, PasswordRecoveryService) => Fragment) = {
    val userDao = prepareUserDaoMock
    val emailSendingService = mock[EmailSendingService]
    val passwordRecoveryService = new PasswordRecoveryService(userDao, emailSendingService)

    test(userDao, emailSendingService, passwordRecoveryService)
  }


  def prepareUserDaoMock = {
    val userDao = new InMemoryUserDAO
    userDao.add(User("user", "user@sml.pl", "pass"))
    userDao
  }

  "sendResetCodeToUser" should {

    withCleanMocks((userDao, emailSendingService, passwordRecoveryService) => {
      "do nothing when login doesn't exist" in {
        passwordRecoveryService.sendResetCodeToUser("user2")
        there was no(emailSendingService).scheduleEmail(anyString, any)
      }

    })

    withCleanMocks((userDao, emailSendingService, passwordRecoveryService) => {
      "send e-mail to user containing link to reset page with generated reset code" in {
        passwordRecoveryService.sendResetCodeToUser("user")
        there was one(emailSendingService).scheduleEmail("user@sml.pl", null)
      }
    })
  }
}
