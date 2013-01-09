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
  }
}
