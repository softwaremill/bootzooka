package com.softwaremill.bootzooka.util

import cats.effect.Sync

import java.time.Instant

/** Any effects that are run as part of transactions and outside of transactions, need to be parametrised with the effect type. */
trait Clock {
  def now[F[_]: Sync](): F[Instant]
}
