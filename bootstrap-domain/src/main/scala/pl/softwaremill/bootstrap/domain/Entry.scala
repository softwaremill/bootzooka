package pl.softwaremill.bootstrap.domain

case class Entry(var id: Int, var author: String, var text: String) {

  def this(text: String) = this(-1, null, text)

  def this(id: Int, text: String) = this(id, null, text)

  def updateWith(entry: Entry) {
    author = entry.author
    text = entry.text
  }
}
