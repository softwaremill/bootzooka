package com.softwaremill.bootzooka.service.user

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.softwaremill.bootzooka.api.Session
import com.softwaremill.bootzooka.dao.RememberMeTokenDao
import com.softwaremill.bootzooka.domain.RememberMeToken
import com.softwaremill.session.{RememberMeData, RememberMeLookupResult, RememberMeStorage}
import org.joda.time.DateTime

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration.{FiniteDuration, Duration}

class RememberMeStorageImpl(dao: RememberMeTokenDao, system: ActorSystem)(implicit ec: ExecutionContext)
    extends RememberMeStorage[Session] {

  override def lookup(selector: String) = {
    dao.findBySelector(selector).map(_.map(t =>
      RememberMeLookupResult(t.tokenHash, t.validTo.getMillis, () => Session(t.userId))))
  }

  override def store(data: RememberMeData[Session]) =
    dao.add(RememberMeToken(UUID.randomUUID(), data.selector, data.tokenHash, data.forSession.userId,
      new DateTime(data.expires)))

  override def remove(selector: String) =
    dao.remove(selector)

  override def schedule[S](after: Duration)(op: => Future[S]) =
    system.scheduler.scheduleOnce(FiniteDuration(after.toSeconds, TimeUnit.SECONDS), new Runnable {
      override def run() = op
    })
}