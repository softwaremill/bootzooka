package pl.softwaremill.bootstrap.service

import data.EntryJson
import pl.softwaremill.bootstrap.domain.Entry
import pl.softwaremill.bootstrap.dao.{UserDAO, EntryDAO}
import org.bson.types.ObjectId
import pl.softwaremill.bootstrap.common.Utils

class EntryService(entryDAO: EntryDAO, userDAO: UserDAO) {

  def loadAll = {
    entryDAO.loadAll.map(e => mapToEntryJson(e))
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

  def add(login: String, message: String) {
    userDAO.findByLogin(login) match {
      case Some(user) => entryDAO.add(Entry(authorId = user._id, text = message))
      case _ =>
    }
  }

  def remove(entryId: String) {
    if (ObjectId.isValid(entryId)) {
      entryDAO.remove(new ObjectId(entryId))
    }
  }

  def load(entryId: String): Option[EntryJson] = {
    ObjectId.isValid(entryId) match {
      case true =>
        entryDAO.load(entryId) match {
          case Some(e) => Option(mapToEntryJson(e))
          case _ => None
        }
      case false =>
        None
    }
  }

  def mapToEntryJson(entry: Entry): EntryJson = {
    EntryJson(entry._id.toString, entry.text, readAuthorLogin(entry.authorId.toString), Utils.format(entry.entered))
  }

  def update(entryId: String, message: String) {
    if (ObjectId.isValid(entryId)) {
      entryDAO.update(entryId, message)
    }
  }

  def isAuthor(login: String, entryId: String): Boolean = {
    if (ObjectId.isValid(entryId)) {
      entryDAO.load(entryId) match {
        case Some(entry) =>
          userDAO.findByLogin(login) match {
            case Some(user) => entry.authorId == user._id
            case _ => false
          }
        case _ => false
      }
    } else {
      false
    }
  }

}
