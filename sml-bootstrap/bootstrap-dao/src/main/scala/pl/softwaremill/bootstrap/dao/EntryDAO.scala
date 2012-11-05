package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.Entry

object EntryDAO {

  // simulates single table in database
  private var list = List(
    Entry(1, "Jan Kowalski", "Short message"),
    Entry(2, "Piotr Nowak", "Very long message"),
    Entry(3, "Krzysztof Jeżyna", "I am from Szczecin")
  )

  var id:Int  = 10

  private def nextId(): Int = {
    id = id + 1
    id
  }

  def loadAll = {
    list
  }

  def count(): Long = {
    list.size
  }

  def add(entry: Entry) {
    entry.id = nextId()
    list = entry +: list
  }

  def remove(entryId: Int) {
    val entryOpt: Option[Entry] = list.find( _.id == entryId )

    entryOpt match {
      case Some(entry) => list = list diff List(entry)
      case _ => { }
    }
  }

  def load(entryId: Int) = {
    val entryOpt: Option[Entry] = list.find( _.id == entryId )

    entryOpt match {
      case Some(entry) => entry
      case _ => Nil
    }
  }

  def update(entry: Entry) {
    val entryOpt: Option[Entry] = list.find( _.id == entry.id )

    entryOpt match {
      case Some(existingEntry) => existingEntry updateWith entry
      case _ => { }
    }
  }


}
