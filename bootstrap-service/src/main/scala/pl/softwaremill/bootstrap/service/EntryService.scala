package pl.softwaremill.bootstrap.service

import data.EntryJson
import pl.softwaremill.bootstrap.domain.Entry
import pl.softwaremill.bootstrap.dao.EntryDAO
import com.mongodb.casbah.Imports._

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
    entryDAO.remove(entryId)
  }

  def load(entryId: String) = {
    EntryJson(entryDAO.load(entryId))
  }

  def update(entry: EntryJson) {
    entryDAO.update(new Entry(new ObjectId(entry.id), entry.author, entry.text))
  }


}
