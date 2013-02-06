package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.PasswordResetCode
import org.bson.types.ObjectId
import org.scalatest.{BeforeAndAfter, FlatSpec}
import org.scalatest.matchers.ShouldMatchers

/**
 * Specification for [[pl.softwaremill.bootstrap.dao.InMemoryPasswordResetCodeDAO]]
 */
class InMemoryPasswordResetCodeDAOSpec extends FlatSpec with ShouldMatchers with BeforeAndAfter {
  behavior of "InMemoryPasswordResetCodeDAO"

  var dao: InMemoryPasswordResetCodeDAO = _

  before {
    dao = new InMemoryPasswordResetCodeDAO
  }

  it should "store code" in {
    val currentCount = dao.count
    val code = PasswordResetCode(code = "code", userId = new ObjectId())
    dao.store(code)
    (dao.count - currentCount) should be (1)
  }

  it should "load stored code" in {
    val code = PasswordResetCode(code = "code", userId = new ObjectId())
    dao.store(code)
    dao.load(code.code)  match {
      case Some(code) => code.code should be ("code")
      case None => fail("Code should be found")
    }
  }

  it should "not load code when not stored" in {
    dao.load("nonexistantcode") match {
      case Some(code) => fail("This code should not be found")
      case None =>
    }
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
    dao.count should be (1)
    dao.load("code1") should be (None)
    dao.load("code2") should be ('defined)
  }
}
