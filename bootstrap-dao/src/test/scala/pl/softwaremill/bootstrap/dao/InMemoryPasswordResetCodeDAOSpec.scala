package pl.softwaremill.bootstrap.dao

import org.specs2.mutable.Specification
import pl.softwaremill.bootstrap.domain.PasswordResetCode
import org.bson.types.ObjectId
import org.joda.time.DateTime

/**
 * Specification for [[pl.softwaremill.bootstrap.dao.InMemoryPasswordResetCodeDAO]]
 */
class InMemoryPasswordResetCodeDAOSpec extends Specification {
  var dao: InMemoryPasswordResetCodeDAO = _

  "InMemoryPasswordResetCodeDAO" should {

    step({
      dao = new InMemoryPasswordResetCodeDAO
    })

    "store code" in {
      val currentCount = dao.count
      val code = new PasswordResetCode(new ObjectId(), "code", new ObjectId())
      dao.store(code)
      dao.count - currentCount === 1
    }
  }
}
