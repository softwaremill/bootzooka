package com.softwaremill.bootzooka.passwordreset

import scala.concurrent.duration.Duration

case class PasswordResetConfig(resetLinkPattern: String, codeValid: Duration) {
  validate()

  def validate(): Unit = {
    val testCode = "TEST_123"
    if (!String.format(resetLinkPattern, testCode).contains(testCode)) {
      throw new IllegalStateException(
        s"Invalid reset link pattern: $resetLinkPattern. Formatting with a test code didn't contain the code in the result."
      )
    }
  }
}
