package pl.softwaremill.bootstrap.service.data

import pl.softwaremill.bootstrap.domain.Entry

case class EntryJson(id: String, var text: String, var author: String)

object EntryJson {
  def apply(entry: Entry) = new EntryJson(entry._id.toString, entry.text, entry.author)

  def apply(list: List[Entry]):List[EntryJson] = {
    for (entry <- list) yield EntryJson(entry)
  }

  def apply(entryOpt: Option[Entry]): Option[EntryJson] = {
    entryOpt.map((e:Entry) => EntryJson(e))
  }
}
