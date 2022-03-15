package com.softwaremill.bootzooka.security

import cats.data.OptionT
import cats.effect.IO
import com.softwaremill.bootzooka._
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.logging.FLogging
import com.softwaremill.bootzooka.user.User
import com.softwaremill.bootzooka.util._
import com.softwaremill.tagging._

import java.security.SecureRandom
import java.time.Instant
import scala.concurrent.duration._

class Auth[T](
    authTokenOps: AuthTokenOps[T],
    xa: Transactor[IO],
    clock: Clock
) extends FLogging {

  // see https://hackernoon.com/hack-how-to-use-securerandom-with-kubernetes-and-docker-a375945a7b21
  private val random = SecureRandom.getInstance("NativePRNGNonBlocking")

  /** Authenticates using the given authentication token. If the token is invalid, a failed [[IO]] is returned, with an instance of the
    * [[Fail]] class. Otherwise, the id of the authenticated user is given.
    */
  def apply(id: Id): IO[Id @@ User] = {
    val tokenOpt = (for {
      token <- OptionT(authTokenOps.findById(id.asId[T]).transact(xa))
      _ <- OptionT(verifyValid(token))
    } yield token).value

    tokenOpt.flatMap {
      case None =>
        logger.debug[IO](s"Auth failed for: ${authTokenOps.tokenName} $id") >>
          // random sleep to prevent timing attacks
          IO.sleep(random.nextInt(1000).millis) >> IO.raiseError(Fail.Unauthorized("Unauthorized"))
      case Some(token) =>
        val delete = if (authTokenOps.deleteWhenValid) authTokenOps.delete(token).transact(xa) else IO.unit
        delete >> IO(authTokenOps.userId(token))
    }
  }

  private def verifyValid(token: T): IO[Option[Unit]] = {
    clock.now[IO]().flatMap { time =>
      if (time.isAfter(authTokenOps.validUntil(token))) {
        logger.info[IO](s"${authTokenOps.tokenName} expired: $token") >>
          authTokenOps.delete(token).transact(xa).map(_ => None)
      } else {
        IO(Some(()))
      }
    }
  }
}

/** A set of operations on an authentication token, which are performed during authentication. Supports both one-time tokens (when
  * `deleteWhenValid=true`) and multi-use tokens.
  */
trait AuthTokenOps[T] {
  def tokenName: String
  def findById: (Id @@ T) => ConnectionIO[Option[T]]
  def delete: T => ConnectionIO[Unit]
  def userId: T => Id @@ User
  def validUntil: T => Instant
  def deleteWhenValid: Boolean
}
