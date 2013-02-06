package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.PasswordResetCode
import org.bson.types.ObjectId
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfterAll

class MongoPasswordResetCodeDAOSpec extends FlatSpecWithMongo with ShouldMatchers with BeforeAndAfterAll {
  behavior of "MongoPasswordResetCodeDAO"

  var dao: MongoPasswordResetCodeDAO = _

  override def beforeAll() {
    super.beforeAll()
    dao = new MongoPasswordResetCodeDAO()
  }

  it should "store code" in {
    val originalCount = dao.count()
    val code = PasswordResetCode(code = "code", userId = new ObjectId())
    dao.store(code)
    (dao.count() - originalCount) should be (1)
  }

  it should "load stored code" in {
    val code = PasswordResetCode(code = "code", userId = new ObjectId())
    dao.store(code)
    dao.load(code.code) match {
      case Some(code) => code.code should be ("code")
      case None => fail("Code should be loaded")
    }
  }

  it should "not load when not stored" in {
    dao.load("code1") match {
      case Some(code) => fail("Code should not be loaded")
      case None =>
    }
  }

  it should "delete code" in {
    //Given
    val count = dao.count()
    val code1: PasswordResetCode = PasswordResetCode(code = "code1", userId = new ObjectId())
    val code2: PasswordResetCode = PasswordResetCode(code = "code2", userId = new ObjectId())
    dao.store(code1)
    dao.store(code2)

    //When
    dao.delete(code1)

    //Then
    (dao.count() - count) should be (1)
    dao.load("code1") should be (None)
    dao.load("code2") should be ('defined)
  }
}
