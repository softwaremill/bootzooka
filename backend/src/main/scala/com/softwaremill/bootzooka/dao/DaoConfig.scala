package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.common.config.ConfigWithDefault
import com.typesafe.config.Config

trait DaoConfig extends ConfigWithDefault {
  def rootConfig: Config

  lazy val dbH2EmbeddedDataDir      = getString("updateimpact.db.h2.data-dir", "./data")
  lazy val dbPostgresConnectionUrl  = getString("updateimpact.db.postgres.connection-url", "")
}
