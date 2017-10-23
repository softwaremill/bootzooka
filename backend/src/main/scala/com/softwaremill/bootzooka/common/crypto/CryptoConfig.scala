package com.softwaremill.bootzooka.common.crypto

import com.softwaremill.bootzooka.common.ConfigWithDefault
import com.typesafe.config.Config

trait CryptoConfig extends ConfigWithDefault {
  def rootConfig: Config

  lazy val iterations  = getInt("bootzooka.crypto.argon2.iterations", 2)
  lazy val memory      = getInt("bootzooka.crypto.argon2.memory", 16383)
  lazy val parallelism = getInt("bootzooka.crypto.argon2.parallelism", 4)
}
