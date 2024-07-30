package com.softwaremill.bootzooka.security

import com.softwaremill.bootzooka.*
import com.softwaremill.bootzooka.infrastructure.Magnum.*
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.user.User
import com.softwaremill.bootzooka.util.*
import com.softwaremill.tagging.*
import ox.{IO, either, sleep}
import ox.either.{fail, ok}

import java.security.SecureRandom
import java.time.Instant
import javax.sql.DataSource
import scala.concurrent.duration.*

class Auth[T](authTokenOps: AuthTokenOps[T], ds: DataSource, clock: Clock) extends Logging:

  // see https://hackernoon.com/hack-how-to-use-securerandom-with-kubernetes-and-docker-a375945a7b21
  private val random = SecureRandom.getInstance("NativePRNGNonBlocking")

  /** Authenticates using the given authentication token. If the token is invalid, a failed [[IO]] is returned, with an instance of the
    * [[Fail]] class. Otherwise, the id of the authenticated user is given.
    */
  def apply(id: Id)(using IO): Either[Fail, Id @@ User] =
    transact(ds)(authTokenOps.findById(id.asId[T])) match {
      case None =>
        logger.debug(s"Auth failed for: ${authTokenOps.tokenName} $id")
        // random sleep to prevent timing attacks
        sleep(random.nextInt(1000).millis)
        Left(Fail.Unauthorized("Unauthorized"))
      case Some(token) if expired(token) =>
        logger.info(s"${authTokenOps.tokenName} expired: $token")
        transact(ds)(authTokenOps.delete(token))
        Left(Fail.Unauthorized("Unauthorized"))
      case Some(token) =>
        if (authTokenOps.deleteWhenValid) transact(ds)(authTokenOps.delete(token))
        Right(authTokenOps.userId(token))
    }

  private def expired(token: T): Boolean = clock.now().isAfter(authTokenOps.validUntil(token))

/** A set of operations on an authentication token, which are performed during authentication. Supports both one-time tokens (when
  * `deleteWhenValid=true`) and multi-use tokens.
  */
trait AuthTokenOps[T]:
  def tokenName: String
  def findById: DbTx ?=> Id @@ T => Option[T]
  def delete: DbTx ?=> T => Unit
  def userId: T => Id @@ User
  def validUntil: T => Instant
  def deleteWhenValid: Boolean
