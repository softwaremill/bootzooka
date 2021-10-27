package com.softwaremill.bootzooka.security

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.free.Free
import com.softwaremill.bootzooka.infrastructure.Doobie

import java.time.Instant
import java.time.temporal.ChronoUnit
import com.softwaremill.bootzooka.infrastructure.Doobie.{ConnectionIO, _}
import com.softwaremill.bootzooka.user.User
import com.softwaremill.bootzooka.util.{Clock, Id, IdGenerator}
import com.softwaremill.tagging.@@
import com.typesafe.scalalogging.StrictLogging
import doobie.free.connection

import scala.concurrent.duration.Duration

class ApiKeyService(apiKeyModel: ApiKeyModel, idGenerator: IdGenerator, clock: Clock) extends StrictLogging {

  def create(userId: Id @@ User, valid: Duration): ConnectionIO[ApiKey] = {
    val apiKeyIO: IO[Doobie.ConnectionIO[ApiKey]] = for {
      id <- idGenerator.nextId[ApiKey]()
      now <- clock.now()
    } yield {
      val validUntil: Instant = now.plus(valid.toMillis, ChronoUnit.MILLIS)
      val apiKey: ApiKey = ApiKey(id, userId, now, validUntil)
      logger.debug(s"Creating a new api key for user $userId, valid until: $validUntil")
      apiKeyModel.insert(apiKey).map(_ => apiKey)
    }
    apiKeyIO.unsafeRunSync()
  }
}

case class ApiKey(id: Id @@ ApiKey, userId: Id @@ User, createdOn: Instant, validUntil: Instant)
