package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.domain.Entry

trait EntryDAO {

  def loadAll: List[Entry]

  def countItems(): Long

  def add(entry: Entry)

  def remove(entryId: String)

  def load(entryId: String): Option[Entry]

  def update(entryId: String, message: String)

  def countNewerThan(timeInMillis: Long): Long

  def loadAuthoredBy(authorId: String): List[Entry]

}
