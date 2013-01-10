package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.PasswordResetCode
import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
 * Specification for [[pl.softwaremill.bootstrap.dao.MongoPasswordResetCodeDAO]]
 */
class MongoPasswordResetCodeDAOSpec extends SpecificationWithMongo {
  var dao: MongoPasswordResetCodeDAO = _

  "MongoPasswordResetCodeDAO" should {
    step {
      dao = new MongoPasswordResetCodeDAO()
    }

    "store code" in {
      val originalCount = dao.count()
      val code = PasswordResetCode(code = "code", userId = new ObjectId())
      dao.store(code)
      dao.count() - originalCount === 1
    }

    "load stored code" in {
      val code = PasswordResetCode(code = "code", userId = new ObjectId())
      dao.store(code)
      dao.load(code.code) match {
        case Some(code) => assert(code.code === "code")
        case None => failure("Code should be loaded")
      }
    }

    "not load when not stored" in {
      dao.load("code1") match {
        case Some(code) => failure("Code should not be loaded")
        case None =>
      }
    }

    "delete code" in {
      //Given
      val count = dao.count()
      val code1: PasswordResetCode = PasswordResetCode(code = "code1", userId = new ObjectId())
      val code2: PasswordResetCode = PasswordResetCode(code = "code2", userId = new ObjectId())
      dao.store(code1)
      dao.store(code2)

      //When
      dao.delete(code1)

      //Then
      assert(dao.count() - count === 1)
      assert(dao.load("code1").isEmpty)
      assert(dao.load("code2").isDefined)
    }
  }
}
