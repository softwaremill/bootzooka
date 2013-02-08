package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.PasswordResetCode
import org.bson.types.ObjectId
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FlatSpec, BeforeAndAfterAll}

class MongoPasswordResetCodeDAOSpec extends FlatSpecWithMongo with PasswordResetCodeDAOSpec {
  behavior of "MongoPasswordResetCodeDAO"

  def createDAO = new MongoPasswordResetCodeDAO()
}

class InMemoryPasswordResetCodeDAOSpec extends FlatSpecWithMongo with PasswordResetCodeDAOSpec {
  behavior of "InMemoryPasswordResetCodeDAO"

  def createDAO = new InMemoryPasswordResetCodeDAO()
}

trait PasswordResetCodeDAOSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {
  def createDAO: PasswordResetCodeDAO

  var dao: PasswordResetCodeDAO = _

  override def beforeAll() {
    super.beforeAll()
    dao = createDAO
  }

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
