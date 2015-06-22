package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.domain.PasswordResetCode
import com.softwaremill.bootzooka.test.{FlatSpecWithSql, UserTestHelpers}
import org.scalatest.Matchers
import org.scalatest.concurrent.IntegrationPatience

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.Random

class PasswordResetCodeDaoSpec extends FlatSpecWithSql with Matchers with UserTestHelpers with IntegrationPatience {
  behavior of "PasswordResetCodeDao"

  val dao = new PasswordResetCodeDao(sqlDatabase)
  val userDao = new UserDao(sqlDatabase)

  def generateRandomUser = {
    val randomLogin = s"${Random.nextInt() * Random.nextPrintableChar()}"
    newUser(randomLogin, s"$randomLogin@example.com", "pass", "someSalt", "someToken")
  }

  it should "add and load code" in {
    // Given
    val code = PasswordResetCode(code = "code", user = generateRandomUser)
    userDao.add(code.user).futureValue

    // When
    dao.add(code).futureValue

    // Then
    dao.load(code.code).futureValue.map(_.code) should be(Some(code.code))
  }

  it should "not load when not added" in {
    dao.load("code1").futureValue should be (None)
  }

  it should "remove code" in {
    //Given
    val code1: PasswordResetCode = PasswordResetCode(code = "code1", user = generateRandomUser)
    val code2: PasswordResetCode = PasswordResetCode(code = "code2", user = generateRandomUser)

    val bgActions = for {
      _ <- userDao.add(code1.user)
      _ <- userDao.add(code2.user)
      _ <- dao.add(code1)
      _ <- dao.add(code2)
    } //When
    yield dao.remove(code1).futureValue

    //Then
    whenReady(bgActions) { _ =>
      dao.load("code1").futureValue should be (None)
      dao.load("code2").futureValue should be ('defined)
    }
  }

  it should "not delete user on code removal" in {
    // Given
    val user = generateRandomUser
    val code = PasswordResetCode(code = "code", user = user)

    val bgActions = for {
      _ <- userDao.add(user)
      _ <- dao.add(code)
    } // When
    yield dao.remove(code)

    // Then
    whenReady(bgActions) { _ =>
      userDao.load(user.id).futureValue should be (Some(user))
    }
  }

}
