package com.softwaremill.bootzooka.service.config

import com.softwaremill.bootzooka.common.config.ConfigWithDefault
import com.typesafe.config.Config

trait CoreConfig extends ConfigWithDefault {
  def rootConfig: Config

  private lazy val topLevelConfig = rootConfig.getConfig("bootzooka")

  lazy val resetLinkPattern = getString("bootzooka.reset-link-pattern", "http://localhost:8080/#/password-reset?code=%s")
}
