package com.softwaremill.bootzooka.common.crypto

trait PasswordHashing {
  def hashPassword(password: String, salt: String): String
  def verifyPassword(hash: String, password: String, salt: String): Boolean
}


