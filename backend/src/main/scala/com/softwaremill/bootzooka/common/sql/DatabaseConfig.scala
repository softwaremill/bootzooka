/*
 * COPYRIGHT (c) 2016 VOCADO, LLC.  ALL RIGHTS RESERVED.  THIS SOFTWARE CONTAINS
 * TRADE SECRETS AND/OR CONFIDENTIAL INFORMATION PROPRIETARY TO VOCADO, LLC AND/OR
 * ITS LICENSORS. ACCESS TO AND USE OF THIS INFORMATION IS STRICTLY LIMITED AND
 * CONTROLLED BY VOCADO, LLC.  THIS SOFTWARE MAY NOT BE COPIED, MODIFIED, DISTRIBUTED,
 * DISPLAYED, DISCLOSED OR USED IN ANY WAY NOT EXPRESSLY AUTHORIZED BY VOCADO, LLC IN WRITING.
 */

package com.softwaremill.bootzooka.common.sql

import com.softwaremill.bootzooka.common.ConfigWithDefault
import com.softwaremill.bootzooka.common.sql.DatabaseConfig._
import com.typesafe.config.Config

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
