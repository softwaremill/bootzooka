package com.softwaremill.bootzooka.user.rememberme

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.softwaremill.bootzooka.user.Session
import com.softwaremill.session.{RememberMeData, RememberMeLookupResult, RememberMeStorage}
import org.joda.time.DateTime

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

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