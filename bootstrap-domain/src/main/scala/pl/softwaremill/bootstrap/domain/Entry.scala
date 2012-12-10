package pl.softwaremill.bootstrap.domain

import com.mongodb.casbah.Imports._
import org.joda.time.DateTime

case class Entry(var _id: ObjectId = new ObjectId, var text: String, var author: String, var entered: DateTime = new DateTime()) {

  def updateWith(entry: Entry) {
    author = entry.author
    text = entry.text
    entered = entry.entered
  }

}
