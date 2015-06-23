package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.common.config.ConfigWithDefault
import com.typesafe.config.Config

trait DaoConfig extends ConfigWithDefault {
  def rootConfig: Config

  // format: OFF
  lazy val dbH2EmbeddedDataDir      = getString("bootzooka.db.h2.data-dir", "./data")
  lazy val dbPostgresConnectionUrl  = getString("bootzooka.db.postgres.connection-url", "")
  // format: ON
}
