package pl.softwaremill.bootstrap.dao

import com.mongodb.casbah.WriteConcern
import com.novus.salat.dao.SalatDAO
import pl.softwaremill.bootstrap.domain.Entry
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.weiglewilczek.slf4s.Logging

class MongoEntryDAO(implicit val mongo: MongoDB) extends SalatDAO[Entry, ObjectId](mongo("entries")) with EntryDAO with Logging {

  RegisterJodaTimeConversionHelpers()

  def loadAll = {
    find(MongoDBObject()).sort(MongoDBObject("_id" -> -1)).toList
  }

  def countItems(): Long = {
    super.count()
  }

  def add(entry: Entry) {
    logger.debug("Adding new entry: " + entry)
    insert(entry, WriteConcern.Safe)
  }

  def remove(entryId: String) {
    remove(MongoDBObject("_id" -> new ObjectId(entryId)))
  }

  def load(entryId: String): Option[Entry] = {
    findOne(MongoDBObject("_id" -> new ObjectId(entryId)))
  }

  def update(entryId: String, message: String) {
    if (ObjectId.isValid(entryId)) {
      load(entryId) match {
        case Some(entry) =>
          entry.text = message
          update(MongoDBObject("_id" -> entry._id), entry, upsert = false, multi = false, wc = WriteConcern.Safe)
        case _ =>
      }
    }
  }

}
