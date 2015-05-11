package com.softwaremill.bootzooka.dao.passwordResetCode

import com.softwaremill.bootzooka.dao.user.SqlUserDao
import com.softwaremill.bootzooka.domain.{PasswordResetCode, User}
import com.softwaremill.bootzooka.test.{ClearSqlDataAfterEach, FlatSpecWithSql}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class SqlPasswordResetCodeDaoSpec extends FlatSpecWithSql with ClearSqlDataAfterEach with ScalaFutures {
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
    whenReady(dao.load(code.code)) {
      codeOpt => codeOpt.map(_.code) should be(Some(code.code))
    }
  }

  it should "not load when not stored" in {
    dao.load("code1") should be (None)
  }

  it should "delete code" in {
    //Given
    val code1: PasswordResetCode = PasswordResetCode(code = "code1", user = generateRandomUser)
    val code2: PasswordResetCode = PasswordResetCode(code = "code2", user = generateRandomUser)

    Future.sequence(Seq(
    userDao.add(code1.user),
    userDao.add(code2.user),
    dao.store(code1),
    dao.store(code2))).futureValue

    //When
    dao.delete(code1).futureValue

    //Then
    dao.load("code1").futureValue should be (None)
    dao.load("code2").futureValue should be ('defined)
  }

  it should "delete all user codes on user removal" in {
    // Given
    val user = generateRandomUser

    val code1 = PasswordResetCode(code = "code1", user = user)
    val code2 = PasswordResetCode(code = "code2", user = user)
    val code3 = PasswordResetCode(code = "code3", user = user)

    Future.sequence(Seq(
      userDao.add(user),
      dao.store(code1),
      dao.store(code2),
      dao.store(code3))).futureValue

    // When
    userDao.remove(user.id)

    // Then
    dao.load(code1.code) should be (None)
    dao.load(code2.code) should be (None)
    dao.load(code3.code) should be (None)
  }

  it should "not delete user on code removal" in {
    // Given
    val user = generateRandomUser
    userDao.add(user).futureValue
    val code = PasswordResetCode(code = "code", user = user)

    dao.store(code).futureValue

    // When
    dao.delete(code).futureValue

    // Then
    userDao.load(user.id).futureValue should be (Some(user))
  }

}
