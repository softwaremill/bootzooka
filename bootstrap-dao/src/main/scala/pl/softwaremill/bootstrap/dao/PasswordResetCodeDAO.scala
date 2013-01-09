package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.PasswordResetCode
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.Imports._
import com.novus.salat.global._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers

/**
 * .
 */
trait PasswordResetCodeDAO {
  def store(code: PasswordResetCode)

  def load(code: String): Option[PasswordResetCode]
}

class InMemoryPasswordResetCodeDAO extends PasswordResetCodeDAO {

  private var codes = List[PasswordResetCode]()

  def store(code: PasswordResetCode) {
    codes ::= code
  }

  def count = {
    codes.length
  }

  def load(code: String): Option[PasswordResetCode] = {
    codes.find(passwordResetCode => {
      passwordResetCode.code == code
    })
  }
}

class MongoPasswordResetCodeDAO(implicit val mongo: MongoDB) extends SalatDAO[PasswordResetCode, ObjectId](mongo("passwordResetCodes")) with PasswordResetCodeDAO {

  RegisterJodaTimeConversionHelpers()

  def store(code: PasswordResetCode) {
    insert(code)
  }

  def load(code: String): Option[PasswordResetCode] = {
    findOne(MongoDBObject("code" -> code))
  }
}