package com.softwaremill.bootzooka.util

import java.time.Instant

import cats.effect.{Clock => CatsClock}
import monix.eval.Task

import scala.concurrent.duration.MILLISECONDS

object DefaultClock extends Clock {

  val clock: CatsClock[Task] = CatsClock.create

  override def now(): Task[Instant] = {
    for {
      now <- clock.realTime(MILLISECONDS)
      instant = Instant.ofEpochMilli(now)
    } yield instant
  }
}
