package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.domain.PasswordResetCode
import org.bson.types.ObjectId
import com.softwaremill.bootzooka.test.FlatSpecWithMongo
import org.scalatest.Matchers

class MongoPasswordResetCodeDAOSpec extends FlatSpecWithMongo with Matchers {
  behavior of "MongoPasswordResetCodeDAO"

  val dao = new MongoPasswordResetCodeDAO()

  it should "store and load code" in {
    // Given
    val code = PasswordResetCode(code = "code", userId = new ObjectId())

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
    val code1: PasswordResetCode = PasswordResetCode(code = "code1", userId = new ObjectId())
    val code2: PasswordResetCode = PasswordResetCode(code = "code2", userId = new ObjectId())
    dao.store(code1)
    dao.store(code2)

    //When
    dao.delete(code1)

    //Then
    dao.load("code1") should be (None)
    dao.load("code2") should be ('defined)
  }
}
