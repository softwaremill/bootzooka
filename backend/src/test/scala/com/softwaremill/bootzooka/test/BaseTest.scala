package com.softwaremill.bootzooka.test

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ox.logback.InheritableMDC

trait BaseTest extends AnyFlatSpec with Matchers:
  InheritableMDC.init
  val testClock = new TestClock()
