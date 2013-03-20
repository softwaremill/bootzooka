package com.softwaremill.bootzooka.service.entry

import com.softwaremill.bootzooka.domain.{User, Entry}
import com.softwaremill.bootzooka.dao.{ UserDAO, EntryDAO }
import com.softwaremill.bootzooka.common.Utils
import com.softwaremill.bootzooka.service.data.{EntriesWithTimeStamp, EntryJson}
import com.softwaremill.common.util.time.{Clock, RealTimeClock}

class EntryService(entryDAO: EntryDAO, userDAO: UserDAO, clock: Clock = new RealTimeClock()) {

  private def findAuthorLogin(entry: Entry, users: List[User]): String = {
    users.find(_.id == entry.authorId).get.login
  }

  def loadAll = {
    val allEntries = entryDAO.loadAll
    val users = userDAO.findForIdentifiers(allEntries.map(_.authorId))
    val entriesJson = allEntries.map(entry => mapToEntryJson(entry, findAuthorLogin(entry, users)))
    EntriesWithTimeStamp(entriesJson, clock )
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
      case Some(user) => entryDAO.add(Entry(message, user.id))
      case _ =>
    }
  }

  def remove(entryId: String) {
    entryDAO.remove(entryId)
  }

  def load(entryId: String): Option[EntryJson] = {
    entryDAO.load(entryId) match {
      case Some(e) => Option(mapToEntryJson(e, readAuthorLogin(e.authorId.toString)))
      case _ => None
    }
  }

  def mapToEntryJson(entry: Entry, login: String): EntryJson = {
    EntryJson(entry.id.toString, entry.text, login, Utils.format(entry.entered))
  }

  def update(entryId: String, message: String) {
    entryDAO.update(entryId, message)
  }

  def isAuthor(login: String, entryId: String): Boolean = {
    entryDAO.load(entryId) match {
      case Some(entry) =>
        userDAO.findByLowerCasedLogin(login) match {
          case Some(user) => entry.authorId == user.id
          case _ => false
        }
      case _ => false
    }
  }

}
