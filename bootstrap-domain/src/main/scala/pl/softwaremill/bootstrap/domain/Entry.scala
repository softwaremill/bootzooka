package pl.softwaremill.bootstrap.domain

import com.mongodb.casbah.Imports._

case class Entry(var _id: ObjectId = new ObjectId, var text: String, var author: String) {

  def updateWith(entry: Entry) {
    author = entry.author
    text = entry.text
  }
}
