package com.softwaremill.bootzooka

import com.softwaremill.tagging._
import tsec.common.SecureRandomId

trait IdGenerator {
  def nextId[U](): Id @@ U
}

object DefaultIdGenerator extends IdGenerator {
  override def nextId[U](): Id @@ U = SecureRandomId.Strong.generate.taggedWith[U]
}
