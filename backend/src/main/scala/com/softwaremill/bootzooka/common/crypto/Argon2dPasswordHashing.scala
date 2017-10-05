package com.softwaremill.bootzooka.common.crypto

import com.typesafe.scalalogging.StrictLogging
import de.mkammerer.argon2.Argon2Factory.Argon2Types
import de.mkammerer.argon2.{Argon2, Argon2Factory}

class Argon2dPasswordHashing extends PasswordHashing with StrictLogging {
  private val argon2: Argon2 = Argon2Factory.create(Argon2Types.ARGON2d)
  private val Iterations     = 2
  private val Memory         = 16383
  private val Parallelism    = 4

  def hashPassword(password: String, salt: String): String =
    argon2.hash(Iterations, Memory, Parallelism, salt + password)

  def verifyPassword(hash: String, password: String, salt: String): Boolean =
    argon2.verify(hash, salt + password)
}
