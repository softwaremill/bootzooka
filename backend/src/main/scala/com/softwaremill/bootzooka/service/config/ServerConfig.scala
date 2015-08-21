package com.softwaremill.bootzooka.service.config

import com.typesafe.config.Config

trait ServerConfig {
  def rootConfig: Config

  lazy val serverHost: String = rootConfig.getString("server.host")
  lazy val serverPort: Int = rootConfig.getInt("server.port")
}
