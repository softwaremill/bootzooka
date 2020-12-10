package com.softwaremill.bootzooka.util

import java.time.Instant

import monix.eval.Task

trait Clock {
  def now(): Task[Instant]
}
