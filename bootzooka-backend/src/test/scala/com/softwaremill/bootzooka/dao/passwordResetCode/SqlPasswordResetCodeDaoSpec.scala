package com.softwaremill.bootzooka.dao.passwordResetCode

import com.softwaremill.bootzooka.dao.user.SqlUserDao
import com.softwaremill.bootzooka.domain.{PasswordResetCode, User}
import com.softwaremill.bootzooka.test.{ClearSqlDataAfterEach, FlatSpecWithSql}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.Random

class SqlPasswordResetCodeDaoSpec extends FlatSpecWithSql with ClearSqlDataAfterEach {
  behavior of "SqlPasswordResetCodeDao"

  val dao = new SqlPasswordResetCodeDao(sqlDatabase)
  val userDao = new SqlUserDao(sqlDatabase)

  def generateRandomUser = {
    val randomLogin = s"${Random.nextInt() * Random.nextPrintableChar()}"
    User(randomLogin, s"$randomLogin@example.com", "pass", "someSalt", "someToken")
  }

  it should "store and load code" in {
    // Given
    val code = PasswordResetCode(code = "code", user = generateRandomUser)
    userDao.add(code.user).futureValue

    // When
    dao.store(code).futureValue

    // Then
    dao.load(code.code).futureValue.map(_.code) should be(Some(code.code))
  }

  it should "not load when not stored" in {
    dao.load("code1").futureValue should be (None)
  }

  it should "delete code" in {
    //Given
    val code1: PasswordResetCode = PasswordResetCode(code = "code1", user = generateRandomUser)
    val code2: PasswordResetCode = PasswordResetCode(code = "code2", user = generateRandomUser)

    val bgActions = for {
      _ <- userDao.add(code1.user)
      _ <- userDao.add(code2.user)
      _ <- dao.store(code1)
      _ <- dao.store(code2)
    }

    //When
    yield dao.delete(code1).futureValue

    //Then
    whenReady(bgActions) { _ =>
      dao.load("code1").futureValue should be (None)
      dao.load("code2").futureValue should be ('defined)
    }
  }

  it should "delete all user codes on user removal" in {
    // Given
    val user = generateRandomUser

    val code1 = PasswordResetCode(code = "code1", user)
    val code2 = PasswordResetCode(code = "code2", user)
    val code3 = PasswordResetCode(code = "code3", user)

    val bgActions = for {
      _ <- userDao.add(user)
      _ <- dao.store(code1)
      _ <- dao.store(code2)
      _ <- dao.store(code3)
    }
    // When
    yield userDao.remove(user.id).futureValue

    // Then
    whenReady(bgActions) { _ =>
      dao.load(code1.code).futureValue should be (None)
      dao.load(code2.code).futureValue should be (None)
      dao.load(code3.code).futureValue should be (None)
    }
  }

  it should "not delete user on code removal" in {
    // Given
    val user = generateRandomUser
    val code = PasswordResetCode(code = "code", user = user)

    val bgActions = for {
      _ <- userDao.add(user)
      _ <- dao.store(code)
    }
    // When
    yield dao.delete(code)

    // Then
    whenReady(bgActions) { _ =>
      userDao.load(user.id).futureValue should be (Some(user))
    }
  }

}
