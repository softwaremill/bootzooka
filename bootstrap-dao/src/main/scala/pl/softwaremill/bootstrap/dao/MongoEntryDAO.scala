package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.Entry
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{DateField, ObjectIdField, ObjectIdPk}
import com.foursquare.rogue.LiftRogue._
import org.joda.time.DateTime
import org.bson.types.ObjectId

class MongoEntryDAO extends EntryDAO {

  import EntryImplicits._

  def loadAll = {
    EntryRecord orderDesc (_.entered) fetch()
  }

  def countItems(): Long = {
    EntryRecord.count
  }

  def add(entry: Entry) {
    entry.save
  }

  def remove(entryId: String) {
    EntryRecord where (_.id eqs new ObjectId(entryId)) findAndDeleteOne()
  }

  def load(entryId: String): Option[Entry] = {
    EntryRecord where (_.id eqs new ObjectId(entryId)) get()
  }

  def update(entryId: String, message: String) {
    EntryRecord where (_.id eqs new ObjectId(entryId)) modify (_.text setTo message) updateOne()
  }

  def countNewerThan(timeInMillis: Long): Long = {
    EntryRecord.where(_.entered after new DateTime(timeInMillis)) count()
  }

  private object EntryImplicits {
    implicit def fromRecord(record: EntryRecord): Entry = {
      Entry(record.id.get, record.text.get, record.authorId.get, new DateTime(record.entered.get))
    }

    implicit def fromRecords(records: List[EntryRecord]): List[Entry] = {
      records map (fromRecord(_))
    }

    implicit def fromOptionalRecord(record: Option[EntryRecord]): Option[Entry] = {
      record map (fromRecord(_))
    }

    implicit def toRecord(entry: Entry): EntryRecord = {
      EntryRecord.createRecord.id(entry.id).text(entry.text).authorId(entry.authorId).entered(entry.entered.toDate)
    }
  }

}

private class EntryRecord extends MongoRecord[EntryRecord] with ObjectIdPk[EntryRecord] {
  def meta = EntryRecord

  object text extends LongStringField(this)

  object authorId extends ObjectIdField(this)

  object entered extends DateField(this)

}

private object EntryRecord extends EntryRecord with MongoMetaRecord[EntryRecord] {
  override def collectionName: String = "entries"
}