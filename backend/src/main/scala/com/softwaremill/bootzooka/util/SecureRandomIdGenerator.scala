package com.softwaremill.bootzooka.util

import java.security.SecureRandom

/** based on TSec https://github.com/jmcardon/tsec */

case class SecureRandomIdGenerator(sizeInBytes: Int):
  /** Cache our random, and seed it properly as per [[https://tersesystems.com/2015/12/17/the-right-way-to-use-securerandom/]] */
  private val cachedRand: SecureRandom =
    val r = SecureRandom.getInstance(if scala.util.Properties.isWin then "Windows-PRNG" else "NativePRNGNonBlocking")
    r.nextBytes(new Array[Byte](20)) // Force reseed
    r

  private def nextBytes(bytes: Array[Byte]): Unit = cachedRand.nextBytes(bytes)

  private def toHexString(byteArray: Array[Byte]) = byteArray.map(b => String.format("%02x", b)).mkString

  def generate: String =
    val byteArray = new Array[Byte](sizeInBytes)
    nextBytes(byteArray)
    toHexString(byteArray)
end SecureRandomIdGenerator

object SecureRandomIdGenerator:
  lazy val Strong: SecureRandomIdGenerator = SecureRandomIdGenerator(32)
  lazy val Interactive: SecureRandomIdGenerator = SecureRandomIdGenerator(16)
