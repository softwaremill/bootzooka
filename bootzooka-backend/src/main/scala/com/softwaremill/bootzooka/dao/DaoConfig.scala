package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.common.config.ConfigWithDefault
import com.typesafe.config.Config

trait DaoConfig extends ConfigWithDefault {
  def rootConfig: Config

  lazy val embeddedDataDir: String = getString("bootzooka.data-dir", "./data")
}
