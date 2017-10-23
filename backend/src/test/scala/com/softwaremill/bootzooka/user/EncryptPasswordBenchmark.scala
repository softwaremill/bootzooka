package com.softwaremill.bootzooka.user

import com.softwaremill.bootzooka.common.crypto.{Argon2dPasswordHashing, CryptoConfig, PasswordHashing, Salt}
import com.softwaremill.bootzooka.common.Utils
import com.typesafe.config.{Config, ConfigFactory}

object EncryptPasswordBenchmark extends App {
  val hashing: PasswordHashing = new Argon2dPasswordHashing(new CryptoConfig {
    override def rootConfig: Config = ConfigFactory.load()
  })

  def timeEncrypting(pass: String, salt: String, iterations: Int): Double = {
    val start = System.currentTimeMillis()
    for (i <- 1 to iterations) {
      hashing.hashPassword(pass, salt)
    }
    val end = System.currentTimeMillis()
    (end - start).toDouble / iterations
  }

  def timeEncryptingAndLog(pass: String, salt: String, iterations: Int) {
    val avg = timeEncrypting(pass, salt, iterations)
    println(s"$iterations iterations of encryption took on average ${avg}ms/encryption")
  }

  val pass = Utils.randomString(32)
  val salt = Salt.newSalt()

  timeEncryptingAndLog(pass, salt, 100) // warmup
  timeEncryptingAndLog(pass, salt, 1000)
  timeEncryptingAndLog(pass, salt, 1000)
}
