package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.Entry
import org.bson.types.ObjectId

class InMemoryEntryDAO extends EntryDAO {

  var entries = List[Entry]()

  def loadAll: List[Entry] = {
    entries
  }

  def countItems(): Long = {
    entries.size
  }

  def add(entry: Entry) {
    entries ::= entry
  }

  def remove(entryId: ObjectId){
    load(entryId) match {
      case Some(entry) => entries.diff(List(entry))
      case _ =>
    }
  }

  def remove(entryId: String) {
    load(entryId) match {
      case Some(entry) => entries.diff(List(entry))
      case _ =>
    }
  }

  def load(entryId: ObjectId): Option[Entry] = {
    entries.find(entry => entry._id == entryId)
  }

  def load(entryId: String): Option[Entry] = {
    entries.find(entry => entry._id == entryId)
  }

  def update(entryId: String, message: String) {
    if (ObjectId.isValid(entryId)) {
      load(entryId) match {
        case Some(e) => entries = entries.updated(entries.indexOf(e), Entry(e._id, message, e.authorId, e.entered))
        case _ =>
      }
    }
  }

}
