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
      val code = new PasswordResetCode(code = "code", userId = new ObjectId())
      dao.store(code)
      dao.count() - originalCount === 1
    }
  }
}
