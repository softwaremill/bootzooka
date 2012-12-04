package pl.softwaremill.bootstrap.service

import data.EntryJson
import pl.softwaremill.bootstrap.domain.Entry
import pl.softwaremill.bootstrap.dao.EntryDAO
import com.mongodb.casbah.Imports._
import org.bson.types.ObjectId

class EntryService(entryDAO: EntryDAO) {

  def loadAll = {
    EntryJson(entryDAO.loadAll)
  }

  def count(): Long = {
    entryDAO.count()
  }

  def add(entry: EntryJson) {
    entryDAO.add(new Entry(author = entry.author, text = entry.text))
  }

  def remove(entryId: String) {
    if (ObjectId.isValid(entryId)) {
      entryDAO.removeEntry(new ObjectId(entryId))
    }
  }

  def load(entryId: String) = {
    if (ObjectId.isValid(entryId)) {
      EntryJson(entryDAO.load(new ObjectId(entryId)))
    }
    else {
      None
    }
  }

  def update(entry: EntryJson) {
    if (ObjectId.isValid(entry.id)) {
      entryDAO.update(new Entry(new ObjectId(entry.id), entry.text, entry.author))
    }
  }
}
