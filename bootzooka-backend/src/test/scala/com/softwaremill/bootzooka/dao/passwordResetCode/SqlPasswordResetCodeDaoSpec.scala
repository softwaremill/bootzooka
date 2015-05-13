package com.softwaremill.bootzooka.dao.passwordResetCode

import com.softwaremill.bootzooka.dao.user.SqlUserDao
import com.softwaremill.bootzooka.domain.{PasswordResetCode, User}
import com.softwaremill.bootzooka.test.{FutureTestSupport, ClearSqlDataAfterEach, FlatSpecWithSql}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class SqlPasswordResetCodeDaoSpec extends FlatSpecWithSql with ClearSqlDataAfterEach with FutureTestSupport {
  behavior of "SqlPasswordResetCodeDao"

  val dao = new SqlPasswordResetCodeDao(sqlDatabase)
  val userDao = new SqlUserDao(sqlDatabase)

  override implicit def ec: ExecutionContext = global

  def generateRandomUser = {
    val randomLogin = s"${Random.nextInt() * Random.nextPrintableChar()}"
    User(randomLogin, s"$randomLogin@example.com", "pass", "someSalt", "someToken")
  }

  it should "store and load code" in {
    // Given
    val code = PasswordResetCode(code = "code", user = generateRandomUser)
    runFutures(
    userDao.add(code.user),

    // When
    dao.store(code))

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

    runFutures(
    userDao.add(code1.user),
    userDao.add(code2.user),
    dao.store(code1),
    dao.store(code2),
    //When
    dao.delete(code1))

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

    runFutures(
      userDao.add(user),
      dao.store(code1),
      dao.store(code2),
      dao.store(code3),

    // When
    userDao.remove(user.id))

    // Then
    dao.load(code1.code).futureValue should be (None)
    dao.load(code2.code).futureValue should be (None)
    dao.load(code3.code).futureValue should be (None)
  }

  it should "not delete user on code removal" in {
    // Given
    val user = generateRandomUser
    val code = PasswordResetCode(code = "code", user = user)

    runFutures(
      userDao.add(user),
      dao.store(code),

      // When
      dao.delete(code))

    // Then
    userDao.load(user.id).futureValue should be (Some(user))
  }

}
