package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.logging.InheritableMDC
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait BaseTest extends AnyFlatSpec with Matchers:
  InheritableMDC.init
  val testClock = new TestClock()
