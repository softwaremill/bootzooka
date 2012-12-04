package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.Entry
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoDB
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.global._

class EntryDAO(implicit mongoConn: MongoDB) extends SalatDAO[Entry, ObjectId](mongoConn("entries")) {

  def loadAll = {
    find(MongoDBObject()).sort(MongoDBObject("_id" -> -1)).toList
  }

  def count(): Long = {
    super.count()
  }

  def add(entry: Entry) {
    insert(entry, WriteConcern.Safe)
  }

  def removeEntry(entryId: ObjectId) {
    remove(MongoDBObject("_id" -> entryId))
  }

  def load(entryId: ObjectId): Option[Entry] = {
    findOne(MongoDBObject("_id" -> entryId))
  }

  def update(entry: Entry) {
    update(MongoDBObject("_id" -> entry._id), entry, false, false, WriteConcern.Safe)
  }
}
