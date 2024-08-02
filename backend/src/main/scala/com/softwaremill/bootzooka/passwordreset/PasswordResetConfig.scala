package com.softwaremill.bootzooka.passwordreset

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

import scala.concurrent.duration.Duration

case class PasswordResetConfig(resetLinkPattern: String, codeValid: Duration) derives ConfigReader:
  validate()

  def validate(): Unit =
    val testCode = "TEST_123"
    if !String.format(resetLinkPattern, testCode).contains(testCode) then
      throw new IllegalStateException(
        s"Invalid reset link pattern: $resetLinkPattern. Formatting with a test code didn't contain the code in the result."
      )
