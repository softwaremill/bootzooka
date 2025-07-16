package com.softwaremill.bootzooka.security

import com.softwaremill.bootzooka.*
import com.softwaremill.bootzooka.infrastructure.DB
import com.softwaremill.bootzooka.infrastructure.Magnum.*
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.user.User
import com.softwaremill.bootzooka.util.*
import com.softwaremill.bootzooka.util.Strings.Id
import ox.sleep

import java.security.SecureRandom
import java.time.Instant
import scala.concurrent.duration.*

class Auth[T](authTokenOps: AuthTokenOps[T], db: DB, clock: Clock) extends Logging:

  // see https://hackernoon.com/hack-how-to-use-securerandom-with-kubernetes-and-docker-a375945a7b21
  private val random = SecureRandom.getInstance("NativePRNGNonBlocking")

  /** Authenticates using the given authentication token. If the token is invalid, a [[Fail.Unauthorized]] error is returned. Otherwise,
    * returns the id of the authenticated user .
    */
  def apply(id: Id[T]): Either[Fail.Unauthorized, Id[User]] =
    db.transact(authTokenOps.findById(id)) match {
      case None =>
        logger.debug(s"Auth failed for: ${authTokenOps.tokenName} $id")
        // random sleep to prevent timing attacks
        sleep(random.nextInt(1000).millis)
        Left(Fail.Unauthorized("Unauthorized"))
      case Some(token) if expired(token) =>
        logger.info(s"${authTokenOps.tokenName} expired: $token")
        db.transact(authTokenOps.delete(token))
        Left(Fail.Unauthorized("Unauthorized"))
      case Some(token) =>
        if (authTokenOps.deleteWhenValid) db.transact(authTokenOps.delete(token))
        Right(authTokenOps.userId(token))
    }

  private def expired(token: T): Boolean = clock.now().isAfter(authTokenOps.validUntil(token))

/** A set of operations on an authentication token, which are performed during authentication. Supports both one-time tokens (when
  * `deleteWhenValid=true`) and multi-use tokens.
  */
trait AuthTokenOps[T]:
  def tokenName: String
  def findById: DbTx ?=> Id[T] => Option[T]
  def delete: DbTx ?=> T => Unit
  def userId: T => Id[User]
  def validUntil: T => Instant
  def deleteWhenValid: Boolean
