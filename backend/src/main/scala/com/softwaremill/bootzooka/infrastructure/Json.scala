package com.softwaremill.bootzooka.infrastructure

import com.softwaremill.bootzooka.util.Id
import com.softwaremill.tagging.@@
import io.circe.generic.AutoDerivation
import io.circe.{Decoder, Encoder, Printer}
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

/** Import the members of this object when doing JSON serialisation or deserialisation.
  */
object Json extends AutoDerivation {
  val noNullsPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  implicit val passwordHashEncoder: Encoder[PasswordHash[SCrypt]] =
    Encoder.encodeString.asInstanceOf[Encoder[PasswordHash[SCrypt]]]

  // can't define a generic encoder because of https://stackoverflow.com/questions/48174799/decoding-case-class-w-tagged-type
  implicit def taggedIdEncoder[U]: Encoder[Id @@ U] = Encoder.encodeString.asInstanceOf[Encoder[Id @@ U]]
  implicit def taggedIdDecoder[U]: Decoder[Id @@ U] = Decoder.decodeString.asInstanceOf[Decoder[Id @@ U]]

  implicit def taggedStringEncoder[U]: Encoder[String @@ U] = Encoder.encodeString.asInstanceOf[Encoder[String @@ U]]
  implicit def taggedStringDecoder[U]: Decoder[String @@ U] = Decoder.decodeString.asInstanceOf[Decoder[String @@ U]]
}
