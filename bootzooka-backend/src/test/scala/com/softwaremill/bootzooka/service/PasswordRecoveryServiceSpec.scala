package com.softwaremill.bootzooka.service

import com.softwaremill.bootzooka.dao.{PasswordResetCodeDAO, UserDAO, InMemoryUserDAO}
import com.softwaremill.bootzooka.domain.{PasswordResetCode, User}
import org.scalatest
import templates.{EmailTemplatingEngine, EmailContentWithSubject}
import org.mockito.Matchers
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.softwaremill.bootzooka.service.email.EmailScheduler
import com.softwaremill.bootzooka.service.config.BootzookaConfig

class PasswordRecoveryServiceSpec extends FlatSpec with scalatest.Matchers with MockitoSugar {
  val invalidLogin = "user2"
  val validLogin = "user"

  def withCleanMocks(test: (UserDAO, PasswordResetCodeDAO, EmailScheduler, PasswordRecoveryService, EmailTemplatingEngine) => Unit) {
    val userDao = prepareUserDaoMock
    val codeDao = mock[PasswordResetCodeDAO]
    val emailScheduler = mock[EmailScheduler]
    val emailTemplatingEngine = mock[EmailTemplatingEngine]
    val passwordRecoveryService = new PasswordRecoveryService(userDao, codeDao, emailScheduler, emailTemplatingEngine,
      new BootzookaConfig {
        override def rootConfig = null
        override lazy val bootzookaResetLinkPattern = "%s"
      })

    test(userDao, codeDao, emailScheduler, passwordRecoveryService, emailTemplatingEngine)
  }

  def prepareUserDaoMock = {
    val userDao = mock[InMemoryUserDAO]
    when (userDao.findByLoginOrEmail(validLogin)) thenReturn Some(User(validLogin, "user@sml.pl", "pass", "salt", "token"))
    when (userDao.findByLoginOrEmail(invalidLogin)) thenReturn None
    userDao
  }

  "sendResetCodeToUser" should "search for user using provided login" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      passwordRecoveryService.sendResetCodeToUser(invalidLogin)
      verify(userDao).findByLoginOrEmail(invalidLogin)
    })
  }

  "sendResetCodeToUser" should "do nothing when login doesn't exist" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      passwordRecoveryService.sendResetCodeToUser(invalidLogin)
      verify(emailSendingService, never()).scheduleEmail(anyString, any[EmailContentWithSubject])
    })
  }

  "sendResetCodeToUser" should "store generated code for reuse" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      passwordRecoveryService.sendResetCodeToUser(validLogin)
      verify(codeDao).store(any[PasswordResetCode])
    })
  }

  "sendResetCodeToUser" should "send e-mail to user containing link to reset page with generated reset code" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      passwordRecoveryService.sendResetCodeToUser(validLogin)
      verify(emailSendingService).scheduleEmail(Matchers.eq("user@sml.pl"), any[EmailContentWithSubject])
    })
  }

  "sendResetCodeToUser" should "use template to generate e-mail" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      passwordRecoveryService.sendResetCodeToUser(validLogin)
      verify(emailTemplatingEngine).passwordReset(Matchers.eq(validLogin), anyString)
    })
  }

  "sendResetCodeToUser" should "change password for user" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      //Given
      val code = "validCode"
      val login = "login"
      val userId = "id"
      val password = "password"
      val salt = "salt"
      val mockUserId = mock[ObjectId]
      val mockCode = mock[PasswordResetCode]
      val mockUser = mock[User]

      given(mockUserId.toString) willReturn userId
      given(mockCode.userId) willReturn mockUserId
      given(mockCode.validTo) willReturn new DateTime().plusHours(1)

      given(mockUser.id) willReturn  mockUserId
      given(mockUser.login) willReturn login
      given(mockUser.salt) willReturn "salt"

      given(codeDao.load(code)) willReturn (Some(mockCode))
      given(userDao.load(userId)) willReturn (Some(mockUser))

      //When
      val result = passwordRecoveryService.performPasswordReset(code, password)

      //Then
      assert(result.isRight)
      assert(result.right.get)
      verify(codeDao).load(code)
      verify(userDao).changePassword(Matchers.eq(userId), Matchers.eq(User.encryptPassword(password, salt)))
      verify(codeDao).delete(mockCode)
    })
  }

  "sendResetCodeToUser" should "not change password when code is past it's valid date" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      //Given
      val password = "password"
      val code = "validCode"
      val mockCode = mock[PasswordResetCode]
      given(mockCode.validTo) willReturn new DateTime().minusDays(2)
      given(codeDao.load(code)) willReturn Some(mockCode)

      //When
      val result = passwordRecoveryService.performPasswordReset(code, password)

      //Then
      assert(result.isLeft)
      verify(codeDao).delete(mockCode)
      verify(userDao, never()).changePassword(anyString, anyString)
    })
  }
}
