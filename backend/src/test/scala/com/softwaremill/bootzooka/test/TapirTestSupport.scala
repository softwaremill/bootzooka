package com.softwaremill.bootzooka.test

import io.circe.{Decoder, parser}
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._

import scala.reflect.ClassTag

trait TapirTestSupport {

  implicit class RichEiter(r: Either[String, String]) {
    def shouldDeserializeTo[T: Decoder: ClassTag]: T =
      r.flatMap(parser.parse).flatMap(_.as[T]).right.get
  }
}
