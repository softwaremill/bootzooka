package com.softwaremill.bootzooka.test

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicReference

import com.softwaremill.bootzooka.util.Clock
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._

class TestClock(nowRef: AtomicReference[Instant]) extends Clock with StrictLogging {

  logger.info(s"New test clock, the time is: ${nowRef.get()}")

  def this(now: Instant) = this(new AtomicReference(now))
  def this() = this(Instant.now())

  override def now(): Instant = nowRef.get()

  def forward(d: Duration): Unit = {
    val newNow = nowRef.get().plus(d.toMillis, ChronoUnit.MILLIS)
    logger.info(s"The time is now $newNow")
    nowRef.set(newNow)
  }
}
