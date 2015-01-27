package com.softwaremill.bootzooka.dao.passwordResetCode

import java.util.UUID

import com.softwaremill.bootzooka.domain.PasswordResetCode
import com.softwaremill.bootzooka.test.{ClearSqlDataAfterEach, FlatSpecWithSql}

class SqlPasswordResetCodeDaoSpec extends FlatSpecWithSql with ClearSqlDataAfterEach {
  behavior of "MongoPasswordResetCodeDao"

  val dao = new SqlPasswordResetCodeDao(sqlDatabase)

  def generateRandomId = UUID.randomUUID()

  it should "store and load code" in {
    // Given
    val code = PasswordResetCode(code = "code", userId = generateRandomId)

    // When
    dao.store(code)

    // Then
    dao.load(code.code).map(_.code) should be (Some(code.code))
  }

  it should "not load when not stored" in {
    dao.load("code1") should be (None)
  }

  it should "delete code" in {
    //Given
    val code1: PasswordResetCode = PasswordResetCode(code = "code1", userId = generateRandomId)
    val code2: PasswordResetCode = PasswordResetCode(code = "code2", userId = generateRandomId)
    dao.store(code1)
    dao.store(code2)

    //When
    dao.delete(code1)

    //Then
    dao.load("code1") should be (None)
    dao.load("code2") should be ('defined)
  }
}
