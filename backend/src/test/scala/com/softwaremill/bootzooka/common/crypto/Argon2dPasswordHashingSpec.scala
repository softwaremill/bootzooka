package com.softwaremill.bootzooka.common.crypto

import com.typesafe.config.Config
import org.scalatest.{FlatSpec, Matchers}

class Argon2dPasswordHashingSpec extends FlatSpec with Matchers {
  val withDefaultParams = new Argon2dPasswordHashing(new CryptoConfig {
    override def rootConfig: Config = ???
    override lazy val iterations = 2
    override lazy val memory = 16383
    override lazy val parallelism = 4
  })

  val withChangedParams = new Argon2dPasswordHashing(new CryptoConfig {
    override def rootConfig: Config = ???
    override lazy val iterations = 3
    override lazy val memory = 16383
    override lazy val parallelism = 2
  })

  behavior of "Argon2d Password Hashing"

  it should "not indicate rehashing necessity when config doesn't change" in {
    val hash = withDefaultParams.hashPassword("password", "salt")

    withDefaultParams.requiresRehashing(hash) shouldBe false
  }

  it should "indicate rehashing necessity upon config change" in {
    val hash = withDefaultParams.hashPassword("password", "salt")

    withChangedParams.requiresRehashing(hash) shouldBe true
  }
}
