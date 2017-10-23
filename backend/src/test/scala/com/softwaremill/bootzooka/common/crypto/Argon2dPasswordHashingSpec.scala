package com.softwaremill.bootzooka.common.crypto

import com.softwaremill.bootzooka.test.TestHelpers
import com.typesafe.config.Config
import org.scalatest.{FlatSpec, Matchers}

class Argon2dPasswordHashingSpec extends FlatSpec with Matchers with TestHelpers {
  val withChangedParams = new Argon2dPasswordHashing(new CryptoConfig {
    override def rootConfig: Config = ???
    override lazy val iterations    = 3
    override lazy val memory        = 16383
    override lazy val parallelism   = 2
  })

  behavior of "Argon2d Password Hashing"

  it should "not indicate rehashing necessity when config doesn't change" in {
    val hash = passwordHashing.hashPassword("password", "salt")

    passwordHashing.requiresRehashing(hash) shouldBe false
  }

  it should "indicate rehashing necessity upon config change" in {
    val hash = passwordHashing.hashPassword("password", "salt")

    withChangedParams.requiresRehashing(hash) shouldBe true
  }
}
