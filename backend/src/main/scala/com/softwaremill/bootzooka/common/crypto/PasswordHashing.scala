package com.softwaremill.bootzooka.common.crypto

trait PasswordHashing {
  def hashPassword(password: String, salt: String): String
  def verifyPassword(hash: String, password: String, salt: String): Boolean

  /**
    * Method that checks whether hash should be rehashed.
    *
    * Typical use-case would be to check parameters used for calculating the hash
    * (for example memory, iterations and parallelism settings in case of Argon2)
    * and check current configuration. If hashing settings were updated since the creation
    * of the hash, it should return true.
    */
  def requiresRehashing(hash: String): Boolean
}
