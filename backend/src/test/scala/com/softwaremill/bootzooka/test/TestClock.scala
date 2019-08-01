package com.softwaremill.bootzooka.test

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import java.util.concurrent.atomic.AtomicReference

import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.Duration

class TestClock(nowRef: AtomicReference[Instant]) extends Clock with StrictLogging {

  logger.info(s"New test clock, the time is: ${nowRef.get()}")

  def this(now: Instant) = this(new AtomicReference(now))
  def this() = this(Instant.now())

  override def getZone: ZoneId = ZoneId.systemDefault()
  override def withZone(zone: ZoneId): Clock = this
  override def instant(): Instant = nowRef.get()

  def forward(d: Duration): Unit = {
    val newNow = nowRef.get().plus(d.toMillis, ChronoUnit.MILLIS)
    logger.info(s"The time is now $newNow")
    nowRef.set(newNow)
  }
}
