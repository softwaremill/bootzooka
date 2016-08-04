package com.softwaremill.bootzooka.passwordreset.application

import java.time.temporal.ChronoUnit
import java.time.{Instant, ZoneOffset}
import java.util.UUID

import com.softwaremill.bootzooka.passwordreset.domain.PasswordResetCode
import com.softwaremill.bootzooka.test.{FlatSpecWithDb, TestHelpersWithDb}
import com.softwaremill.bootzooka.user.domain.User
import com.typesafe.config.ConfigFactory

class PasswordResetServiceSpec extends FlatSpecWithDb with TestHelpersWithDb {

  lazy val config = new PasswordResetConfig {
    override def rootConfig = ConfigFactory.load()
  }
  val passwordResetCodeDao = new PasswordResetCodeDao(sqlDatabase)
  val passwordResetService = new PasswordResetService(userDao, passwordResetCodeDao, emailService, emailTemplatingEngine, config)

  "sendResetCodeToUser" should "do nothing when login doesn't exist" in {
    passwordResetService.sendResetCodeToUser("Does not exist").futureValue
  }

  "performPasswordReset" should "delete code after it was used once" in {
    // given
    val user = newRandomStoredUser()
    val code = PasswordResetCode(randomString(), user)
    passwordResetCodeDao.add(code).futureValue

    val newPassword1 = randomString()
    val newPassword2 = randomString()

    // when
    val result1 = passwordResetService.performPasswordReset(code.code, newPassword1).futureValue
    val result2 = passwordResetService.performPasswordReset(code.code, newPassword2).futureValue

    result1 should be ('right)
    result2 should be ('left)

    User.passwordsMatch(newPassword1, userDao.findById(user.id).futureValue.get) should be (true)
    User.passwordsMatch(newPassword2, userDao.findById(user.id).futureValue.get) should be (false)

    passwordResetCodeDao.findByCode(code.code).futureValue should be (None)
  }

  "performPasswordReset" should "delete code and do nothing if the code expired" in {
    // given
    val user = newRandomStoredUser()
    val previousDay = Instant.now().minus(24, ChronoUnit.HOURS).atOffset(ZoneOffset.UTC)
    val code = PasswordResetCode(UUID.randomUUID(), randomString(), user, previousDay)
    passwordResetCodeDao.add(code).futureValue

    val newPassword = randomString()

    // when
    val result = passwordResetService.performPasswordReset(code.code, newPassword).futureValue

    result should be ('left)
    User.passwordsMatch(newPassword, userDao.findById(user.id).futureValue.get) should be (false)
    passwordResetCodeDao.findByCode(code.code).futureValue should be (None)
  }
}
