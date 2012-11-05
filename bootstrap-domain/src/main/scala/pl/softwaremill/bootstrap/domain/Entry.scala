package pl.softwaremill.bootstrap.domain

case class Entry(var id: Int, var author: String, var text: String) {

  def this(author: String, text: String) = this(-1, author, text)

  def updateWith(entry: Entry) {
    author = entry.author
    text = entry.text
  }

  override def toString = {
    "[Entry: id = " + id + ", text = " + text +", author = " + author +"]";
  }
}
