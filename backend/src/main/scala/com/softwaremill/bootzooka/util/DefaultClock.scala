package com.softwaremill.bootzooka.util

import java.time.Instant

import cats.effect.Clock
import monix.eval.Task

import scala.concurrent.duration.MILLISECONDS

object DefaultClock extends ClockProvider {

  val clock: Clock[Task] = Clock.create

  override def now(): Task[Instant] = {
    for {
      now <- clock.realTime(MILLISECONDS)
      instant = Instant.ofEpochMilli(now)
    } yield instant
  }
}
