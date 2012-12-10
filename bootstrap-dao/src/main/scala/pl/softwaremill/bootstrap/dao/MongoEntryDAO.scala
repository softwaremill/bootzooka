package pl.softwaremill.bootstrap.dao

import com.mongodb.casbah.{WriteConcern, MongoDB}
import com.novus.salat.dao.SalatDAO
import pl.softwaremill.bootstrap.domain.Entry
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.global._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers

class MongoEntryDAO(implicit val mongo: MongoDB) extends SalatDAO[Entry, ObjectId](mongo("entries")) with EntryDAO {

  RegisterJodaTimeConversionHelpers()

  def loadAll = {
    find(MongoDBObject()).sort(MongoDBObject("_id" -> -1)).toList
  }

  def countItems(): Long = {
    super.count()
  }

  def add(entry: Entry) {
    insert(entry, WriteConcern.Safe)
  }

  def remove(entryId: ObjectId) {
    remove(MongoDBObject("_id" -> entryId))
  }

  def remove(entryId: String) {
    remove(MongoDBObject("_id" -> new ObjectId(entryId)))
  }

  def load(entryId: String): Option[Entry] = {
    findOne(MongoDBObject("_id" -> new ObjectId(entryId)))
  }

  def load(entryId: ObjectId): Option[Entry] = {
    findOne(MongoDBObject("_id" -> entryId))
  }

  def update(entry: Entry) {
    update(MongoDBObject("_id" -> entry._id), entry, false, false, WriteConcern.Safe)
  }

}
