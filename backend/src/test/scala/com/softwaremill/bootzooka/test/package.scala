package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.config.{Config, ConfigModule}

import scala.concurrent.duration._

package object test {
  val DefaultConfig: Config = new ConfigModule {}.config
  val TestConfig: Config = DefaultConfig.copy(email = DefaultConfig.email.copy(emailSendInterval = 100.milliseconds))
}
