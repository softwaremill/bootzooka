package pl.softwaremill.bootstrap.domain

import com.mongodb.casbah.Imports._
import org.joda.time.DateTime

case class Entry(var _id: ObjectId = new ObjectId, var text: String, var authorId: ObjectId, var entered: DateTime = new DateTime())
