package com.softwaremill.bootzooka.util

import cats.effect.Sync
import java.security.SecureRandom

/** copy-pasted from TSec https://github.com/jmcardon/tsec */

/** A trait that manages a secureRandom instance.
  */
trait ManagedRandom {

  /** Cache our random, and seed it properly as per [[https://tersesystems.com/2015/12/17/the-right-way-to-use-securerandom/]]
    */
  private val cachedRand: SecureRandom = {
    val r =
      SecureRandom.getInstance(if (scala.util.Properties.isWin) ManagedRandom.WinRandom else ManagedRandom.UnixURandom)
    r.nextBytes(new Array[Byte](20)) // Force reseed
    r
  }

  def nextBytes(bytes: Array[Byte]): Unit =
    cachedRand.nextBytes(bytes)
}

object ManagedRandom {
  private[ManagedRandom] val WinRandom = "Windows-PRNG"
  private[ManagedRandom] val UnixURandom = "NativePRNGNonBlocking"
}

object SecureRandomId {
  lazy val Strong: SecureRandomIdGenerator = SecureRandomIdGenerator(32)
  lazy val Interactive: SecureRandomIdGenerator = SecureRandomIdGenerator(16)

  def apply(s: String): SecureRandomId = s.asInstanceOf[SecureRandomId]
}

case class SecureRandomIdGenerator(sizeInBytes: Int = 32) extends ManagedRandom {
  def generate: SecureRandomId = {
    val byteArray = new Array[Byte](sizeInBytes)
    nextBytes(byteArray)
    toHexString(byteArray).asInstanceOf[SecureRandomId]
  }

  private def toHexString(byteArray: Array[Byte]) = byteArray.map(b => String.format("%02x", b)).mkString

  def generateF[F[_]](implicit F: Sync[F]): F[SecureRandomId] = F.delay(generate)
}
