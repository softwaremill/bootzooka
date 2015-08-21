package com.softwaremill.bootzooka.api

import java.util.UUID

import com.softwaremill.session.{ToMapSessionSerializer, SessionSerializer}

case class Session(userId: UUID)

object Session {
  implicit val serializer: SessionSerializer[Session] = new ToMapSessionSerializer[Session] {
    override def serializeToMap(t: Session) = Map("id" -> t.userId.toString)
    override def deserializeFromMap(m: Map[String, String]) = Session(UUID.fromString(m("id")))
  }
}