package com.softwaremill.bootzooka

import java.util.Locale
import cats.data.NonEmptyList
import cats.effect.IO
import com.softwaremill.tagging._
import sttp.tapir.server.ServerEndpoint

package object util {
  type SecureRandomId <: String
  type Id = SecureRandomId

  implicit class RichString(val s: String) extends AnyVal {
    def asId[T]: Id @@ T = s.asInstanceOf[Id @@ T]
    def lowerCased: String @@ LowerCased = s.toLowerCase(Locale.ENGLISH).taggedWith[LowerCased]
    def hashedPassword: String @@ PasswordHash = s.taggedWith[PasswordHash]
  }

  type ServerEndpoints = NonEmptyList[ServerEndpoint[Any, IO]]
}
