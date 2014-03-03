package com.softwaremill.bootzooka

import com.typesafe.config.Config

trait EmbeddedJettyConfig {
  def rootConfig: Config

  lazy val webServerHost: String = rootConfig.getString("embedded-jetty.host")
  lazy val webServerPort: Int = rootConfig.getInt("embedded-jetty.port")
}
