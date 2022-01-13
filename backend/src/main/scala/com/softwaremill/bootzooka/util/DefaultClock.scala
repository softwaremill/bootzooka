package com.softwaremill.bootzooka.util

import cats.effect.{Sync, Clock => CatsClock}
import cats.syntax.all._

import java.time.Instant

object DefaultClock extends Clock {

  override def now[F[_]: Sync](): F[Instant] = {
    for {
      now <- CatsClock[F].realTime.map(_.length)
      instant = Instant.ofEpochMilli(now)
    } yield instant
  }
}
