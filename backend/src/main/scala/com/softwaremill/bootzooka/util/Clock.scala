package com.softwaremill.bootzooka.util

import cats.effect.IO

import java.time.Instant

trait Clock {
  def now(): IO[Instant]
}
