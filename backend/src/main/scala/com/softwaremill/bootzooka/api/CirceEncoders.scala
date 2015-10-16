package com.softwaremill.bootzooka.api

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes.ClientError
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

trait CirceEncoders {

  val dateTimeFormat = ISODateTimeFormat.basicDateTime()

  implicit object DateTimeEncoder extends Encoder[DateTime] {
    override def apply(dt: DateTime): Json = dateTimeFormat.print(dt).asJson
  }

  implicit object UuidEncoder extends Encoder[UUID] {
    override def apply(u: UUID): Json = u.toString.asJson
  }

  implicit object ClientErrorEncoder extends Encoder[ClientError] {
    override def apply(a: ClientError): Json = a.defaultMessage.asJson
  }
}