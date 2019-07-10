package com.softwaremill

import java.util.Locale

import cats.data.NonEmptyList
import com.softwaremill.tagging._
import monix.eval.Task
import tapir.server.ServerEndpoint
import tsec.common.SecureRandomId

package object bootzooka {
  type Id = SecureRandomId

  implicit class RichString(val s: String) extends AnyVal {
    def asId[T]: Id @@ T = s.asInstanceOf[Id @@ T]
    def lowerCased: String @@ LowerCased = s.toLowerCase(Locale.ENGLISH).taggedWith[LowerCased]
  }

  type ServerEndpoints = NonEmptyList[ServerEndpoint[_, _, _, Nothing, Task]]
}
