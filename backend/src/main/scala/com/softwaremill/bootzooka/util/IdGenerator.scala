package com.softwaremill.bootzooka.util

import com.softwaremill.tagging._
import monix.eval.Task
import tsec.common.SecureRandomId

trait IdGenerator {
  def nextId[U](): Task[Id @@ U]
}

object DefaultIdGenerator extends IdGenerator {
  override def nextId[U](): Task[Id @@ U] = Task { SecureRandomId.Strong.generate.taggedWith[U] }
}
