package com.softwaremill.bootzooka.util

import cats.effect.IO
import com.softwaremill.tagging._
import tsec.common.SecureRandomId

trait IdGenerator {
  def nextId[U](): IO[Id @@ U]
}

object DefaultIdGenerator extends IdGenerator {
  override def nextId[U](): IO[Id @@ U] = IO { SecureRandomId.Strong.generate.taggedWith[U] }
}
