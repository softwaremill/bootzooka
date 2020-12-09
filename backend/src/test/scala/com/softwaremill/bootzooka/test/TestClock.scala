package com.softwaremill.bootzooka.test

import java.time.Instant

import cats.effect.Clock
import cats.effect.laws.util.TestContext
import com.softwaremill.bootzooka.util.ClockProvider
import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS}

class TestClock extends ClockProvider with StrictLogging {

  implicit lazy val ec: TestContext = TestContext.apply()
  implicit lazy val clock: Clock[Task] = ec.timer[Task].clock

  logger.info(s"New test clock, the time is: ${now().runToFuture}")

  def forward(d: Duration): Unit = {
    ec.tick(FiniteDuration(d.toMillis, MILLISECONDS))
    val newNow = clock.realTime(MILLISECONDS)
    newNow.foreach(t => logger.info(s"The time is now ${Instant.ofEpochMilli(t)}") )
  }

  def now(): Task[Instant] = {
    for {
      now <- clock.realTime(MILLISECONDS)
      instant = Instant.ofEpochMilli(now)
    } yield instant
  }
}
