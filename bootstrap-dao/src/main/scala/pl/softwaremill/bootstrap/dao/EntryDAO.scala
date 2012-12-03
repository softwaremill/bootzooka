package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.Entry
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoDB
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.global._

class EntryDAO(implicit mongoConn: MongoDB) extends SalatDAO[Entry, ObjectId](mongoConn("entries")) {

  def loadAll = {
    find(MongoDBObject()).toList
  }

  def count(): Long = {
    super.count()
  }

  def add(entry: Entry) {
    insert(entry, WriteConcern.Safe)
  }

  def remove(entryId: String) {
    remove(MongoDBObject("_id" -> new ObjectId(entryId)))
  }

  def load(entryId: String): Option[Entry] = {
    findOne(MongoDBObject("_id" -> new ObjectId(entryId)))
  }

  def update(entry: Entry) {
    update(MongoDBObject("_id" -> entry._id), entry, false, false, WriteConcern.Safe)
  }
}
