package com.softwaremill.bootzooka.user

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

import scala.concurrent.duration.Duration

case class UserConfig(defaultApiKeyValid: Duration) derives ConfigReader
