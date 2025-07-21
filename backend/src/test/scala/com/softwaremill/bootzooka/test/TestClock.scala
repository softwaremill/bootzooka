package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.logging.Logging

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicReference
import com.softwaremill.bootzooka.util.Clock

import scala.concurrent.duration.Duration

class TestClock(nowRef: AtomicReference[Instant]) extends Clock with Logging:
  logger.info(s"New test clock, the time is: ${nowRef.get()}")

  def this(now: Instant) = this(new AtomicReference(now))

  def this() = this(Instant.now())

  def forward(d: Duration): Unit =
    val newNow = nowRef.get().plus(d.toMillis, ChronoUnit.MILLIS)
    logger.info(s"The time is now $newNow")
    nowRef.set(newNow)

  override def now(): Instant = nowRef.get()
end TestClock
