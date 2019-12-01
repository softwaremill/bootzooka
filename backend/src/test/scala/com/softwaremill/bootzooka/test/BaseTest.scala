package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.infrastructure.CorrelationId
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait BaseTest extends AnyFlatSpec with Matchers {
  CorrelationId.init()
  val testClock = new TestClock()
}
