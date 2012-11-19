package pl.softwaremill.bootstrap.service

import pl.softwaremill.bootstrap.domain.Entry
import pl.softwaremill.bootstrap.dao.EntryDAO

class EntryService(entryDAO: EntryDAO) {

  def loadAll = {
    entryDAO.loadAll
  }

  def count(): Long = {
    entryDAO.count()
  }

  def add(entry: Entry) {
    entryDAO.add(entry)
  }

  def remove(entryId: Int) {
    entryDAO.remove(entryId)
  }

  def load(entryId: Int) = {
    entryDAO.load(entryId)
  }

  def update(entry: Entry) {
    entryDAO.update(entry)
  }


}
