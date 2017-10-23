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
  val passwordResetService =
    new PasswordResetService(
      userDao,
      passwordResetCodeDao,
      emailService,
      emailTemplatingEngine,
      config,
      passwordHashing
    )

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

    result1 should be('right)
    result2 should be('left)

    val updatedUser = userDao.findById(user.id).futureValue.get
    passwordHashing.verifyPassword(updatedUser.password, newPassword1, updatedUser.salt) should be(true)
    passwordHashing.verifyPassword(updatedUser.password, newPassword2, updatedUser.salt) should be(false)

    passwordResetCodeDao.findByCode(code.code).futureValue should be(None)
  }

  "performPasswordReset" should "delete code and do nothing if the code expired" in {
    // given
    val user        = newRandomStoredUser()
    val previousDay = Instant.now().minus(24, ChronoUnit.HOURS).atOffset(ZoneOffset.UTC)
    val code        = PasswordResetCode(UUID.randomUUID(), randomString(), user, previousDay)
    passwordResetCodeDao.add(code).futureValue

    val newPassword = randomString()

    // when
    val result = passwordResetService.performPasswordReset(code.code, newPassword).futureValue

    result should be('left)
    val updatedUser = userDao.findById(user.id).futureValue.get
    passwordHashing.verifyPassword(updatedUser.password, newPassword, updatedUser.salt) should be(false)
    passwordResetCodeDao.findByCode(code.code).futureValue should be(None)
  }

  "performPasswordReset" should "calculate different hash values for the same passwords" in {
    // given
    val password             = randomString()
    val user                 = newRandomStoredUser(Some(password))
    val originalPasswordHash = userDao.findById(user.id).futureValue.get.password
    val code                 = PasswordResetCode(randomString(), user)
    passwordResetCodeDao.add(code).futureValue

    // when
    val result = passwordResetService.performPasswordReset(code.code, password).futureValue

    result should be('right)

    val newPasswordHash = userDao.findById(user.id).futureValue.get.password

    originalPasswordHash should not be equal(newPasswordHash)
  }
}
