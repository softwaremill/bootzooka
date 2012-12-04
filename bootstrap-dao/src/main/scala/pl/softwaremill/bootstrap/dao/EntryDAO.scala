package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.Entry
import org.bson.types.ObjectId

trait EntryDAO {

  def loadAll: List[Entry]

  def countItems(): Long

  def add(entry: Entry)

  def remove(entryId: ObjectId)

  def remove(entryId: String)

  def load(entryId: String): Option[Entry]

  def load(entryId: ObjectId): Option[Entry]

  def update(entry: Entry)

}
