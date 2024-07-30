package com.softwaremill.bootzooka.util

import com.softwaremill.tagging._

trait IdGenerator:
  def nextId[U](): Id @@ U // TODO: opaque type instead of @@?

object DefaultIdGenerator extends IdGenerator:
  override def nextId[U](): Id @@ U = SecureRandomId.Strong.generate.taggedWith[U]
