package com.softwaremill.bootzooka.security

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}

import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.user.User
import com.softwaremill.bootzooka.util.{Id, IdGenerator}
import com.softwaremill.tagging.@@
import com.typesafe.scalalogging.StrictLogging
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration.Duration

class ApiKeyService(apiKeyModel: ApiKeyModel, idGenerator: IdGenerator, clock: Clock) extends StrictLogging {

  def create(userId: Id @@ User, valid: Duration): ConnectionIO[ApiKey] = {
    val now = clock.instant()
    val validUntil = now.plus(valid.toMinutes, ChronoUnit.MINUTES)
    for {
      id <- idGenerator.nextId[ApiKey]().to[ConnectionIO]
      apiKey = ApiKey(id, userId, now, validUntil)
      _ = logger.debug(s"Creating a new api key for user $userId, valid until: $validUntil")
      apiKey <- apiKeyModel.insert(apiKey).map(_ => apiKey)
    } yield apiKey

  }
}

case class ApiKey(id: Id @@ ApiKey, userId: Id @@ User, createdOn: Instant, validUntil: Instant)
