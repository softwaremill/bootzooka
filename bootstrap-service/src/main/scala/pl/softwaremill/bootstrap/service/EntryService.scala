package pl.softwaremill.bootstrap.service

import pl.softwaremill.bootstrap.domain.Entry
import pl.softwaremill.bootstrap.dao.EntryDAO

object EntryService {

  def loadAll = {
    EntryDAO.loadAll
  }

  def count(): Long = {
    EntryDAO.count()
  }

  def add(entry: Entry) {
    EntryDAO.add(entry)
  }

  def remove(entryId: Int) {
    EntryDAO.remove(entryId)
  }

  def load(entryId: Int) = {
    EntryDAO.load(entryId)
  }

  def update(entry: Entry) {
    EntryDAO.update(entry)
  }


}
