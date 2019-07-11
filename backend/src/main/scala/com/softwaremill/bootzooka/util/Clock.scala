package com.softwaremill.bootzooka.util

import java.time.Instant

trait Clock {
  def now(): Instant
}

object DefaultClock extends Clock {
  override def now(): Instant = Instant.now()
}
