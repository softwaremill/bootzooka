package com.softwaremill.bootzooka.util

import cats.effect.Sync
import java.security.SecureRandom
import org.apache.commons.codec.binary.Hex

trait ManagedRandom {

  private val cachedRand: SecureRandom = {
    val r =
      SecureRandom.getInstance(if (scala.util.Properties.isWin) ManagedRandom.WinRandom else ManagedRandom.UnixURandom)
    r.nextBytes(new Array[Byte](20)) //Force reseed
    r
  }

  def nextBytes(bytes: Array[Byte]): Unit =
    cachedRand.nextBytes(bytes)
}

object ManagedRandom {
  private[ManagedRandom] val WinRandom   = "Windows-PRNG"
  private[ManagedRandom] val UnixURandom = "NativePRNGNonBlocking"
}

object SecureRandomId {
  lazy val Strong: SecureRandomIdGenerator      = SecureRandomIdGenerator(32)
  lazy val Interactive: SecureRandomIdGenerator = SecureRandomIdGenerator(16)

  def apply(s: String): SecureRandomId  = s.asInstanceOf[SecureRandomId]
  def coerce(s: String): SecureRandomId = s.asInstanceOf[SecureRandomId]
}

case class SecureRandomIdGenerator(sizeInBytes: Int = 32) extends ManagedRandom {
  def generate: SecureRandomId = {
    val byteArray = new Array[Byte](sizeInBytes)
    nextBytes(byteArray)
    new String(Hex.encodeHex(byteArray)).asInstanceOf[SecureRandomId]
  }

  def generateF[F[_]](implicit F: Sync[F]): F[SecureRandomId] = F.delay(generate)
}
