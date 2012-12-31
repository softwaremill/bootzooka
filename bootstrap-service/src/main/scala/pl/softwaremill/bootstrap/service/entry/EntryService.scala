package pl.softwaremill.bootstrap.service.entry

import pl.softwaremill.bootstrap.domain.Entry
import pl.softwaremill.bootstrap.dao.{ UserDAO, EntryDAO }
import pl.softwaremill.bootstrap.common.Utils
import pl.softwaremill.bootstrap.service.data.{EntriesWithTimeStamp, EntryJson}

class EntryService(entryDAO: EntryDAO, userDAO: UserDAO) {

  def loadAll = {
    val entriesJson = entryDAO.loadAll.map(e => mapToEntryJson(e))

    EntriesWithTimeStamp(entriesJson)
  }

  def readAuthorLogin(userId: String): String = {
    userDAO.load(userId) match {
      case Some(u) => u.login
      case _ => ""
    }
  }

  def count(): Long = {
    entryDAO.countItems()
  }

  def countNewerThan(timeInMillis: Long): Long = {
    entryDAO.countNewerThan(timeInMillis)
  }

  def add(login: String, message: String) {
    userDAO.findByLowerCasedLogin(login) match {
      case Some(user) => entryDAO.add(Entry(authorId = user._id, text = message))
      case _ =>
    }
  }

  def remove(entryId: String) {
    entryDAO.remove(entryId)
  }

  def load(entryId: String): Option[EntryJson] = {
    entryDAO.load(entryId) match {
      case Some(e) => Option(mapToEntryJson(e))
      case _ => None
    }
  }

  def mapToEntryJson(entry: Entry): EntryJson = {
    EntryJson(entry._id.toString, entry.text, readAuthorLogin(entry.authorId.toString), Utils.format(entry.entered))
  }

  def update(entryId: String, message: String) {
    entryDAO.update(entryId, message)
  }

  def isAuthor(login: String, entryId: String): Boolean = {
    entryDAO.load(entryId) match {
      case Some(entry) =>
        userDAO.findByLowerCasedLogin(login) match {
          case Some(user) => entry.authorId == user._id
          case _ => false
        }
      case _ => false
    }
  }

}
