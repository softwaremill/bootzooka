package com.softwaremill.bootzooka.util

import java.time.Instant
import cats.effect.{Clock => CatsClock, IO}

object DefaultClock extends Clock {

  override def now(): IO[Instant] = {
    for {
      now <- CatsClock[IO].realTime.map(_.length)
      instant = Instant.ofEpochMilli(now)
    } yield instant
  }
}
