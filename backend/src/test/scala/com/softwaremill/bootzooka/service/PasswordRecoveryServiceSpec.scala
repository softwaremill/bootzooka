package com.softwaremill.bootzooka.service

import java.util.UUID
import com.softwaremill.bootzooka.dao.{UserDao, PasswordResetCodeDao}
import com.softwaremill.bootzooka.domain.{PasswordResetCode, User}
import com.softwaremill.bootzooka.service.config.CoreConfig
import com.softwaremill.bootzooka.service.email.EmailService
import com.softwaremill.bootzooka.service.templates.{EmailContentWithSubject, EmailTemplatingEngine}
import com.softwaremill.bootzooka.test.UserTestHelpers
import org.joda.time.DateTime
import org.mockito.BDDMockito._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest
import org.scalatest.FlatSpec
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Promise, Future}

class PasswordRecoveryServiceSpec extends FlatSpec with scalatest.Matchers with MockitoSugar with ScalaFutures
    with IntegrationPatience with UserTestHelpers {
  val invalidLogin = "user2"
  val validLogin = "user"

  def generateRandomId = UUID.randomUUID()

  def withCleanMocks(test: (UserDao, PasswordResetCodeDao, EmailService, PasswordRecoveryService, EmailTemplatingEngine) => Unit) {
    val userDao = prepareUserDaoMock
    val codeDao = mock[PasswordResetCodeDao]
    val emailService = mock[EmailService]
    val emailTemplatingEngine = mock[EmailTemplatingEngine]
    val passwordRecoveryService = new PasswordRecoveryService(userDao, codeDao, emailService, emailTemplatingEngine,
      new CoreConfig {
        override def rootConfig = null
        override lazy val resetLinkPattern = "%s"
      })

    test(userDao, codeDao, emailService, passwordRecoveryService, emailTemplatingEngine)
  }

  def prepareUserDaoMock = {
    val userDao = mock[UserDao]
    when (userDao.findByLoginOrEmail(validLogin)) thenReturn Future {
      Some(newUser(validLogin, "user@sml.pl", "pass", "salt", "token"))
    }
    when (userDao.findByLoginOrEmail(invalidLogin)) thenReturn Future { None }
    userDao
  }

  "sendResetCodeToUser" should "search for user using provided login" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      // given
      given(codeDao.add(any[PasswordResetCode])).willReturn(Future.successful(()))
      given(emailSendingService.scheduleEmail(any[String], any[EmailContentWithSubject])).willReturn(Future.successful(()))
      // when
      passwordRecoveryService.sendResetCodeToUser(invalidLogin).futureValue
      // then
      verify(userDao).findByLoginOrEmail(invalidLogin)
    })
  }

  "sendResetCodeToUser" should "do nothing when login doesn't exist" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      // given
      given(codeDao.add(any[PasswordResetCode])).willReturn(Future.successful(()))
      // when
      passwordRecoveryService.sendResetCodeToUser(invalidLogin).futureValue
      // then
      verify(emailSendingService, never()).scheduleEmail(anyString, any[EmailContentWithSubject])
    })
  }

  "sendResetCodeToUser" should "store generated code for reuse" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      // given
      given(codeDao.add(any[PasswordResetCode])).willReturn(Future.successful(()))
      given(emailSendingService.scheduleEmail(any[String], any[EmailContentWithSubject])).willReturn(Future.successful(()))
      // when
      passwordRecoveryService.sendResetCodeToUser(validLogin).futureValue
      // then
      verify(codeDao).add(any[PasswordResetCode])
    })
  }

  "sendResetCodeToUser" should "send e-mail to user containing link to reset page with generated reset code" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      // given
      given(codeDao.add(any[PasswordResetCode])).willReturn(Future.successful(()))
      given(emailSendingService.scheduleEmail(any[String], any[EmailContentWithSubject])).willReturn(Future.successful(()))
      // when
      passwordRecoveryService.sendResetCodeToUser(validLogin).futureValue
      // then
      verify(emailSendingService).scheduleEmail(Matchers.eq("user@sml.pl"), any[EmailContentWithSubject])
    })
  }

  "sendResetCodeToUser" should "use template to generate e-mail" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      // given
      given(codeDao.add(any[PasswordResetCode])).willReturn(Future.successful(()))
      given(emailSendingService.scheduleEmail(any[String], any[EmailContentWithSubject])).willReturn(Future.successful(()))
      // when
      passwordRecoveryService.sendResetCodeToUser(validLogin).futureValue
      // then
      verify(emailTemplatingEngine).passwordReset(Matchers.eq(validLogin), anyString)
    })
  }

  "sendResetCodeToUser" should "change password for user" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      // given
      val code = "validCode"
      val login = "login"
      val password = "password"
      val salt = "salt"
      val user = newUser(login, s"$login@example.com", password, salt, "someRandomToken")
      val resetCode = PasswordResetCode(UUID.randomUUID(), code, user, new DateTime().plusHours(1))

      given(codeDao.load(code)) willReturn Future { Some(resetCode) }
      given(userDao.changePassword(any[UserDao#UserId], any[String])).willReturn(Future.successful(()))
      given(codeDao.remove(resetCode)).willReturn(Future.successful(()))

      // when
      val result = passwordRecoveryService.performPasswordReset(code, password).futureValue

      // then
      assert(result.isRight)
      assert(result.right.get)
      verify(codeDao).load(code)
      verify(userDao).changePassword(Matchers.eq(user.id), Matchers.eq(User.encryptPassword(password, salt)))
      verify(codeDao).remove(resetCode)
    })
  }

  "sendResetCodeToUser" should "do nothing but delete expired code" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      //Given
      val code = "validCode"
      val login = "login"
      val password = "password"
      val salt = "salt"
      val user = newUser(login, s"$login@example.com", password, salt, "someRandomToken")
      val resetCode = PasswordResetCode(UUID.randomUUID(), code, user, new DateTime().minusHours(1))

      given(codeDao.load(code)) willReturn Future { Some(resetCode) }
      given(codeDao.remove(resetCode)).willReturn(Future.successful(()))

      //When
      val result = passwordRecoveryService.performPasswordReset(code, password).futureValue

      //Then
      assert(result.isLeft)
      assert(result.left.get == "Your reset code is invalid. Please try again.")
      verify(codeDao).load(code)
      verify(userDao, never).changePassword(Matchers.eq(user.id), Matchers.eq(User.encryptPassword(password, salt)))
      verify(codeDao).remove(resetCode)
    })
  }

  "sendResetCodeToUser" should "not change password when code is past it's valid date" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordRecoveryService, emailTemplatingEngine) => {
      //Given
      val password = "password"
      val code = "validCode"
      val mockCode = mock[PasswordResetCode]
      given(mockCode.validTo) willReturn new DateTime().minusDays(2)
      given(codeDao.load(code)) willReturn Future { Some(mockCode) }
      given(codeDao.remove(mockCode)).willReturn(Future.successful(()))

      //When
      val result = passwordRecoveryService.performPasswordReset(code, password).futureValue

      //Then
      assert(result.isLeft)
      verify(codeDao).remove(mockCode)
      verify(userDao, never()).changePassword(any[UUID], anyString)
    })
  }
}
