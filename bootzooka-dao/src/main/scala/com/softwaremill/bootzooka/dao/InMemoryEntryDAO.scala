package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.domain.Entry

class InMemoryEntryDAO extends EntryDAO {

  var entries = List[Entry]()

  def loadAll: List[Entry] = {
    entries.sortBy(- _.entered.getMillis)
  }

  def countItems(): Long = {
    entries.size
  }

  def add(entry: Entry) {
    entries ::= entry
  }

  def remove(entryId: String) {
    load(entryId) match {
      case Some(entry) => entries = entries.diff(List(entry))
      case _ =>
    }
  }

  def load(entryId: String): Option[Entry] = {
    entries.find(entry => entry.id == entryId)
  }

  def update(entryId: String, message: String) {
    load(entryId) match {
      case Some(e) => entries = entries.updated(entries.indexOf(e), e.copy(text = message))
      case _ =>
    }
  }

  def countNewerThan(timeInMillis: Long): Long = {
    entries.filter(e => e.entered.isAfter(timeInMillis)).size
  }

  def loadAuthoredBy(authorId: String): List[Entry] = {
    entries.filter(_.authorId  == authorId).sortBy(- _.entered.getMillis)
  }

}
