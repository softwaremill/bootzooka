package com.softwaremill.bootzooka.security

import java.time.{Clock, Instant}
import java.time.temporal.ChronoUnit

import com.softwaremill.bootzooka.user.User
import com.softwaremill.tagging.@@
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.util.{Id, IdGenerator}
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task

import scala.concurrent.duration.Duration

class ApiKeyService(apiKeyModel: ApiKeyModel, idGenerator: IdGenerator, clock: Clock) extends StrictLogging {

  def create(userId: Id @@ User, valid: Duration): Task[ConnectionIO[ApiKey]] = {
    val now = clock.instant()
    val validUntil = now.plus(valid.toMinutes, ChronoUnit.MINUTES)
    for {
      id <- idGenerator.nextId[ApiKey]()
      apiKey = ApiKey(id, userId, now, validUntil)
      _ = logger.debug(s"Creating a new api key for user $userId, valid until: $validUntil")

    } yield apiKeyModel.insert(apiKey).map(_ => apiKey)

  }
}

case class ApiKey(id: Id @@ ApiKey, userId: Id @@ User, createdOn: Instant, validUntil: Instant)
