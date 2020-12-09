package com.softwaremill.bootzooka.util

import java.time.Instant

import monix.eval.Task

trait ClockProvider {
  def now(): Task[Instant]
}
