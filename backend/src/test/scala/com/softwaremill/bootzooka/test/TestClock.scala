package com.softwaremill.bootzooka.test

import java.time.Instant

import cats.effect.laws.util.TestContext
import cats.effect.{Clock, IO}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS}

class TestClock extends StrictLogging {

  implicit lazy val ec: TestContext = TestContext.apply()
  implicit lazy val clock: Clock[IO] = ec.timer[IO].clock

  logger.info(s"New test clock, the time is: ${now().unsafeRunSync()}")

  def forward(d: Duration): Unit = {
    ec.tick(FiniteDuration(d.toMillis, MILLISECONDS))
    val newNow = clock.realTime(MILLISECONDS)
    newNow.flatMap(t => IO { logger.info(s"The time is now ${Instant.ofEpochMilli(t)}") }).unsafeRunSync()
  }

  def now(): IO[Instant] = {
    for {
      now <- clock.realTime(MILLISECONDS)
      instant = Instant.ofEpochMilli(now)
    } yield instant
  }
}
