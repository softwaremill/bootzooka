package com.softwaremill.bootzooka.common

import com.softwaremill.bootzooka.common.Utils._
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.matchers.ShouldMatchers

class UtilsSpec extends FlatSpec with Matchers {
  behavior of "checkbox()"

  it should "convert 'true' to boolean true" in {
    checkbox("true") should be (true)
  }

  it should "convert 'tRuE' to boolean true" in {
    checkbox("tRuE") should be (true)
  }

  it should "convert null to boolean false" in {
    checkbox(null) should be (false)
  }

  behavior of "sha1"

  it should "generate proper hash" in {
    sha1("admin") should not be (null)
  }

  it should "generate string of length 40" in {
    sha1("admin") should have length (40)
  }

  behavior of "sha256"

  it should "generate proper hash" in {
    sha256("admin", "secret") should not be (null)
  }

  it should "generate string of length 64" in {
    sha256("admin", "secret") should have length (64)
  }
}
