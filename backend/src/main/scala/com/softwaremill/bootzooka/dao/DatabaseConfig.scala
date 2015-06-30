package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.common.config.ConfigWithDefault
import com.typesafe.config.Config
import DatabaseConfig._

trait DatabaseConfig extends ConfigWithDefault {
  def rootConfig: Config

  // format: OFF
  lazy val dbH2Url              = getString(s"bootzooka.db.h2.properties.url", "jdbc:h2:file:./data/bootzooka")
  lazy val dbPostgresServerName = getString(PostgresServerNameKey, "")
  lazy val dbPostgresPort       = getString(PostgresPortKey, "5432")
  lazy val dbPostgresDbName     = getString(PostgresDbNameKey, "")
  lazy val dbPostgresUsername   = getString(PostgresUsernameKey, "")
  lazy val dbPostgresPassword   = getString(PostgresPasswordKey, "")
}

object DatabaseConfig {
  val PostgresDSClass       = "bootzooka.db.postgres.dataSourceClass"
  val PostgresServerNameKey = "bootzooka.db.postgres.properties.serverName"
  val PostgresPortKey       = "bootzooka.db.postgres.properties.portNumber"
  val PostgresDbNameKey     = "bootzooka.db.postgres.properties.databaseName"
  val PostgresUsernameKey   = "bootzooka.db.postgres.properties.user"
  val PostgresPasswordKey   = "bootzooka.db.postgres.properties.password"
  // format: ON
}