package com.softwaremill.bootzooka.user.application

import java.time.{Instant, ZoneOffset}
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.softwaremill.bootzooka.user.domain.RememberMeToken
import com.softwaremill.session.{RefreshTokenData, RefreshTokenLookupResult, RefreshTokenStorage}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

class RefreshTokenStorageImpl(dao: RememberMeTokenDao, system: ActorSystem)(implicit ec: ExecutionContext)
    extends RefreshTokenStorage[Session] {

  override def lookup(selector: String) = {
    dao.findBySelector(selector).map(_.map(t =>
      RefreshTokenLookupResult(t.tokenHash, t.validTo.toInstant.toEpochMilli, () => Session(t.userId))))
  }

  override def store(data: RefreshTokenData[Session]) =
    dao.add(RememberMeToken(UUID.randomUUID(), data.selector, data.tokenHash, data.forSession.userId,
      Instant.ofEpochMilli(data.expires).atOffset(ZoneOffset.UTC)))

  override def remove(selector: String) =
    dao.remove(selector)

  override def schedule[S](after: Duration)(op: => Future[S]) =
    system.scheduler.scheduleOnce(FiniteDuration(after.toSeconds, TimeUnit.SECONDS), new Runnable {
      override def run() = op
    })
}
