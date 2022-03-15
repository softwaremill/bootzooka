package com.softwaremill.bootzooka.security

import com.softwaremill.bootzooka.infrastructure.Doobie.ConnectionIO
import com.softwaremill.bootzooka.logging.FLogging
import com.softwaremill.bootzooka.user.User
import com.softwaremill.bootzooka.util.{Clock, Id, IdGenerator}
import com.softwaremill.tagging.@@

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.duration.Duration

class ApiKeyService(apiKeyModel: ApiKeyModel, idGenerator: IdGenerator, clock: Clock) extends FLogging {

  def create(userId: Id @@ User, valid: Duration): ConnectionIO[ApiKey] =
    for {
      id <- idGenerator.nextId[ConnectionIO, ApiKey]()
      now <- clock.now[ConnectionIO]()
      validUntil: Instant = now.plus(valid.toMillis, ChronoUnit.MILLIS)
      apiKey: ApiKey = ApiKey(id, userId, now, validUntil)
      _ <- logger.debug[ConnectionIO](s"Creating a new api key for user $userId, valid until: $validUntil")
      _ <- apiKeyModel.insert(apiKey)
    } yield apiKey
}

case class ApiKey(id: Id @@ ApiKey, userId: Id @@ User, createdOn: Instant, validUntil: Instant)
