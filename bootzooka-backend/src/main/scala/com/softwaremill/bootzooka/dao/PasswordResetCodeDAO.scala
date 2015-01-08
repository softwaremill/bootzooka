package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.domain.PasswordResetCode
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{ObjectIdField, ObjectIdPk}
import net.liftweb.record.field.DateTimeField
import org.joda.time.DateTime
import com.foursquare.rogue.LiftRogue._
import java.util.Locale

import scala.language.implicitConversions

trait PasswordResetCodeDAO {

  def store(code: PasswordResetCode)

  def load(code: String): Option[PasswordResetCode]

  def delete(code: PasswordResetCode)
}

class InMemoryPasswordResetCodeDAO extends PasswordResetCodeDAO {

  private var codes = List[PasswordResetCode]()

  def store(code: PasswordResetCode) {
    codes ::= code
  }

  def load(code: String): Option[PasswordResetCode] = {
    codes.find(passwordResetCode => {
      passwordResetCode.code == code
    })
  }

  def delete(code: PasswordResetCode) {
    val index = codes.indexOf(code)
    codes = codes.take(index) ::: codes.drop(index + 1)
  }
}

class MongoPasswordResetCodeDAO extends PasswordResetCodeDAO {

  import PasswordResetCodeImplicits._

  def store(code: PasswordResetCode) {
    code.save(safe = false)
  }

  def load(code: String): Option[PasswordResetCode] = {
    PasswordResetCodeRecord where (_.code eqs code) get()
  }

  def delete(code: PasswordResetCode) {
    PasswordResetCodeRecord where (_.id eqs code.id) findAndDeleteOne()
  }

  private object PasswordResetCodeImplicits {
    implicit def fromRecord(record: PasswordResetCodeRecord): PasswordResetCode = {
      PasswordResetCode(record.id.get, record.code.get, record.userId.get, new DateTime(record.validTo.get))
    }

    implicit def fromOptionalRecord(record: Option[PasswordResetCodeRecord]): Option[PasswordResetCode] = {
      record map (fromRecord(_))
    }

    implicit def toRecord(code: PasswordResetCode): PasswordResetCodeRecord = {
      PasswordResetCodeRecord.createRecord.id(code.id).code(code.code).userId(code.userId).validTo(code.validTo.toCalendar(Locale.getDefault))
    }
  }

}

private class PasswordResetCodeRecord extends MongoRecord[PasswordResetCodeRecord] with ObjectIdPk[PasswordResetCodeRecord] {
  def meta = PasswordResetCodeRecord

  object code extends LongStringField(this)

  object userId extends ObjectIdField(this)

  object validTo extends DateTimeField(this)

}

private object PasswordResetCodeRecord extends PasswordResetCodeRecord with MongoMetaRecord[PasswordResetCodeRecord] {
  override def collectionName: String = "passwordResetCodes"
}