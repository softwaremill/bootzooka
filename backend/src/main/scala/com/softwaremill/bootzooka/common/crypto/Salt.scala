package com.softwaremill.bootzooka.common.crypto

import com.softwaremill.bootzooka.common.Utils

object Salt {
  //the default salt length is 128 bits
  val DefaultSaltLength = 16

  /**
    * Generates a new salt.
    *
    * Uses characters 33-126 from ASCII table.
    * @param length the length of the salt
    * @return string with generated salt
    */
  def newSalt(length: Int = DefaultSaltLength): String = Utils.randomString(length, 93, 33)
}
