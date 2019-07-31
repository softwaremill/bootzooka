package com.softwaremill.bootzooka.passwordreset

import scala.concurrent.duration.Duration

case class PasswordResetConfig(resetLinkPattern: String, codeValid: Duration)
