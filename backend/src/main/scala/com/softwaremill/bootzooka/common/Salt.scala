package com.softwaremill.bootzooka.common

object Salt {
  val DefaultSaltLength = 128

  def newSalt(length: Int = DefaultSaltLength): String = Utils.randomString(length)
}
