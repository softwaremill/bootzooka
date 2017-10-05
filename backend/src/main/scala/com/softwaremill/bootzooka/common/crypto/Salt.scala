package com.softwaremill.bootzooka.common.crypto

import com.softwaremill.bootzooka.common.Utils

object Salt {
  val DefaultSaltLength = 128

  def newSalt(length: Int = DefaultSaltLength): String = Utils.randomString(length)
}
