package com.softwaremill.bootzooka.security

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.softwaremill.bootzooka.user.User
import com.softwaremill.tagging.@@
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.util.{Clock, Id, IdGenerator}
import com.typesafe.scalalogging.StrictLogging

class ApiKeyService(idGenerator: IdGenerator, clock: Clock) extends StrictLogging {

  def create(userId: Id @@ User, validHours: Int): ConnectionIO[ApiKey] = {
    val now = clock.now()
    val validUntil = now.plus(validHours.toLong, ChronoUnit.HOURS)
    val apiKey = ApiKey(idGenerator.nextId[ApiKey](), userId, clock.now(), validUntil)

    logger.debug(s"Creating a new api key for user $userId, valid until: $validUntil")
    ApiKeyModel.insert(apiKey).map(_ => apiKey)
  }
}

case class ApiKey(id: Id @@ ApiKey, userId: Id @@ User, createdOn: Instant, validUntil: Instant)
