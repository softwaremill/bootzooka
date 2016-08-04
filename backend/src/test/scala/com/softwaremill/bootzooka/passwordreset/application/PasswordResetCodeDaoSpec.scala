package com.softwaremill.bootzooka.passwordreset.application

import com.softwaremill.bootzooka.passwordreset.domain.PasswordResetCode
import com.softwaremill.bootzooka.test.{FlatSpecWithDb, TestHelpersWithDb}

class PasswordResetCodeDaoSpec extends FlatSpecWithDb with TestHelpersWithDb {
  behavior of "PasswordResetCodeDao"

  val dao = new PasswordResetCodeDao(sqlDatabase)

  it should "add and load code" in {
    // Given
    val user = newRandomStoredUser()
    val code = PasswordResetCode(code = "code", user = user)

    // When
    dao.add(code).futureValue

    // Then
    dao.findByCode(code.code).futureValue.map(_.code) should be(Some(code.code))
  }

  it should "not load when not added" in {
    dao.findByCode("code1").futureValue should be (None)
  }

  it should "remove code" in {
    //Given
    val user1 = newRandomStoredUser()
    val user2 = newRandomStoredUser()

    val code1 = PasswordResetCode(code = "code1", user = user1)
    val code2 = PasswordResetCode(code = "code2", user = user2)

    val bgActions = for {
      _ <- dao.add(code1)
      _ <- dao.add(code2)
    } //When
    yield dao.remove(code1).futureValue

    //Then
    whenReady(bgActions) { _ =>
      dao.findByCode("code1").futureValue should be (None)
      dao.findByCode("code2").futureValue should be ('defined)
    }
  }

  it should "not delete user on code removal" in {
    // Given
    val user = newRandomStoredUser()
    val code = PasswordResetCode(code = "code", user = user)

    val bgActions = for {
      _ <- dao.add(code)
    } // When
    yield dao.remove(code)

    // Then
    whenReady(bgActions) { _ =>
      userDao.findById(user.id).futureValue should be (Some(user))
    }
  }

}
