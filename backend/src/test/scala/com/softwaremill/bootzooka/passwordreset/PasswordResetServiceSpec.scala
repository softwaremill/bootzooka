package com.softwaremill.bootzooka.passwordreset

import java.time.temporal.ChronoUnit
import java.time.{Instant, ZoneOffset}
import java.util.UUID

import com.softwaremill.bootzooka.config.CoreConfig
import com.softwaremill.bootzooka.email.{EmailContentWithSubject, EmailService, EmailTemplatingEngine}
import com.softwaremill.bootzooka.test.TestHelpers
import com.softwaremill.bootzooka.user.{User, UserDao, UserId}
import org.mockito.BDDMockito._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest
import org.scalatest.FlatSpec
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mock.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PasswordResetServiceSpec extends FlatSpec with scalatest.Matchers with MockitoSugar with ScalaFutures
    with IntegrationPatience with TestHelpers {
  val invalidLogin = "user2"
  val validLogin = "user"

  def generateRandomId = UUID.randomUUID()

  def withCleanMocks(test: (UserDao, PasswordResetCodeDao, EmailService, PasswordResetService, EmailTemplatingEngine) => Unit) {
    val userDao = prepareUserDaoMock
    val codeDao = mock[PasswordResetCodeDao]
    val emailService = mock[EmailService]
    val emailTemplatingEngine = mock[EmailTemplatingEngine]
    val passwordresetService = new PasswordResetService(userDao, codeDao, emailService, emailTemplatingEngine,
      new CoreConfig {
        override def rootConfig = null
        override lazy val resetLinkPattern = "%s"
      })

    test(userDao, codeDao, emailService, passwordresetService, emailTemplatingEngine)
  }

  def prepareUserDaoMock = {
    val userDao = mock[UserDao]
    when (userDao.findByLoginOrEmail(validLogin)) thenReturn Future {
      Some(newUser(validLogin, "user@sml.pl", "pass", "salt"))
    }
    when (userDao.findByLoginOrEmail(invalidLogin)) thenReturn Future { None }
    userDao
  }

  "sendResetCodeToUser" should "search for user using provided login" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordresetService, emailTemplatingEngine) => {
      // given
      given(codeDao.add(any[PasswordResetCode])).willReturn(Future.successful(()))
      given(emailSendingService.scheduleEmail(any[String], any[EmailContentWithSubject])).willReturn(Future.successful(()))
      // when
      passwordresetService.sendResetCodeToUser(invalidLogin).futureValue
      // then
      verify(userDao).findByLoginOrEmail(invalidLogin)
    })
  }

  "sendResetCodeToUser" should "do nothing when login doesn't exist" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordresetService, emailTemplatingEngine) => {
      // given
      given(codeDao.add(any[PasswordResetCode])).willReturn(Future.successful(()))
      // when
      passwordresetService.sendResetCodeToUser(invalidLogin).futureValue
      // then
      verify(emailSendingService, never()).scheduleEmail(anyString, any[EmailContentWithSubject])
    })
  }

  "sendResetCodeToUser" should "store generated code for reuse" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordresetService, emailTemplatingEngine) => {
      // given
      given(codeDao.add(any[PasswordResetCode])).willReturn(Future.successful(()))
      given(emailSendingService.scheduleEmail(any[String], any[EmailContentWithSubject])).willReturn(Future.successful(()))
      // when
      passwordresetService.sendResetCodeToUser(validLogin).futureValue
      // then
      verify(codeDao).add(any[PasswordResetCode])
    })
  }

  "sendResetCodeToUser" should "send e-mail to user containing link to reset page with generated reset code" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordresetService, emailTemplatingEngine) => {
      // given
      given(codeDao.add(any[PasswordResetCode])).willReturn(Future.successful(()))
      given(emailSendingService.scheduleEmail(any[String], any[EmailContentWithSubject])).willReturn(Future.successful(()))
      // when
      passwordresetService.sendResetCodeToUser(validLogin).futureValue
      // then
      verify(emailSendingService).scheduleEmail(Matchers.eq("user@sml.pl"), any[EmailContentWithSubject])
    })
  }

  "sendResetCodeToUser" should "use template to generate e-mail" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordresetService, emailTemplatingEngine) => {
      // given
      given(codeDao.add(any[PasswordResetCode])).willReturn(Future.successful(()))
      given(emailSendingService.scheduleEmail(any[String], any[EmailContentWithSubject])).willReturn(Future.successful(()))
      // when
      passwordresetService.sendResetCodeToUser(validLogin).futureValue
      // then
      verify(emailTemplatingEngine).passwordReset(Matchers.eq(validLogin), anyString)
    })
  }

  "sendResetCodeToUser" should "change password for user" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordresetService, emailTemplatingEngine) => {
      // given
      val code = "validCode"
      val login = "login"
      val password = "password"
      val salt = "salt"
      val user = newUser(login, s"$login@example.com", password, salt)
      val resetCode = PasswordResetCode(UUID.randomUUID(), code, user, Instant.now().plus(1, ChronoUnit.HOURS).atOffset(ZoneOffset.UTC))

      given(codeDao.findByCode(code)) willReturn Future { Some(resetCode) }
      given(userDao.changePassword(any[UserId], any[String])).willReturn(Future.successful(()))
      given(codeDao.remove(resetCode)).willReturn(Future.successful(()))

      // when
      val result = passwordresetService.performPasswordReset(code, password).futureValue

      // then
      assert(result.isRight)
      assert(result.right.get)
      verify(codeDao).findByCode(code)
      verify(userDao).changePassword(Matchers.eq(user.id), Matchers.eq(User.encryptPassword(password, salt)))
      verify(codeDao).remove(resetCode)
    })
  }

  "sendResetCodeToUser" should "do nothing but delete expired code" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordresetService, emailTemplatingEngine) => {
      //Given
      val code = "validCode"
      val login = "login"
      val password = "password"
      val salt = "salt"
      val user = newUser(login, s"$login@example.com", password, salt)
      val resetCode = PasswordResetCode(UUID.randomUUID(), code, user, Instant.now().minus(1, ChronoUnit.HOURS).atOffset(ZoneOffset.UTC))

      given(codeDao.findByCode(code)) willReturn Future { Some(resetCode) }
      given(codeDao.remove(resetCode)).willReturn(Future.successful(()))

      //When
      val result = passwordresetService.performPasswordReset(code, password).futureValue

      //Then
      assert(result.isLeft)
      assert(result.left.get == "Your reset code is invalid. Please try again.")
      verify(codeDao).findByCode(code)
      verify(userDao, never).changePassword(Matchers.eq(user.id), Matchers.eq(User.encryptPassword(password, salt)))
      verify(codeDao).remove(resetCode)
    })
  }

  "sendResetCodeToUser" should "not change password when code is past it's valid date" in {
    withCleanMocks((userDao, codeDao, emailSendingService, passwordresetService, emailTemplatingEngine) => {
      //Given
      val password = "password"
      val code = "validCode"
      val mockCode = mock[PasswordResetCode]
      given(mockCode.validTo) willReturn Instant.now().minus(2, ChronoUnit.DAYS).atOffset(ZoneOffset.UTC)
      given(codeDao.findByCode(code)) willReturn Future { Some(mockCode) }
      given(codeDao.remove(mockCode)).willReturn(Future.successful(()))

      //When
      val result = passwordresetService.performPasswordReset(code, password).futureValue

      //Then
      assert(result.isLeft)
      verify(codeDao).remove(mockCode)
      verify(userDao, never()).changePassword(any[UUID], anyString)
    })
  }
}
