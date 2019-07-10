package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.infrastructure.CorrelationId
import org.scalatest.{FlatSpec, Matchers}

trait BaseTest extends FlatSpec with Matchers {
  CorrelationId.init()
  val testClock = new TestClock()
}
