package com.softwaremill.bootzooka.passwordreset

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

import scala.concurrent.duration.Duration

case class PasswordResetConfig(resetLinkPattern: String, codeValid: Duration) derives ConfigReader:
  validate()

  def validate(): Unit =
    val testCode = "TEST_123"
    assert(
      String.format(resetLinkPattern, testCode).contains(testCode),
      s"Invalid reset link pattern: $resetLinkPattern. Formatting with a test code didn't contain the code in the result."
    )
