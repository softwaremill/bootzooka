package com.softwaremill.bootzooka.domain

import com.softwaremill.bootzooka.common.Utils

// Run this locally to determine the desired number of iterations in PBKDF2
object EncryptPasswordBenchmark extends App {
  def timeEncrypting(pass: String, salt: String, iterations: Int): Double = {
    val start = System.currentTimeMillis()
    for (i <- 1 to iterations) {
      User.encryptPassword(pass, salt)
    }
    val end = System.currentTimeMillis()
    (end - start).toDouble / iterations
  }

  def timeEncryptingAndLog(pass: String, salt: String, iterations: Int) {
    val avg = timeEncrypting(pass, salt, iterations)
    println(s"$iterations iterations of encryption took on average ${avg}ms/encryption")
  }

  val pass = Utils.randomString(32)
  val salt = Utils.randomString(128)

  timeEncryptingAndLog(pass, salt, 100) // warmup
  timeEncryptingAndLog(pass, salt, 1000)
  timeEncryptingAndLog(pass, salt, 1000)
}
