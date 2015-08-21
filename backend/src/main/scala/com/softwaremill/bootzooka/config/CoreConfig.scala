package com.softwaremill.bootzooka.config

import com.softwaremill.bootzooka.common.ConfigWithDefault
import com.typesafe.config.Config

trait CoreConfig extends ConfigWithDefault {
  def rootConfig: Config

  lazy val resetLinkPattern = getString("bootzooka.reset-link-pattern", "http://localhost:8080/#/password-reset?code=%s")
}
