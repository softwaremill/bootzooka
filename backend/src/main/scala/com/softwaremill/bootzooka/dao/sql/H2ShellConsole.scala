package com.softwaremill.bootzooka.dao.sql

import com.softwaremill.bootzooka.dao.DaoConfig
import com.typesafe.config.ConfigFactory

object H2ShellConsole extends App {
  val config = new DaoConfig {
    def rootConfig = ConfigFactory.load()
  }

  println("Note: when selecting from tables, enclose the table name in \" \".")
  new org.h2.tools.Shell().runTool("-url", SqlDatabase.connectionString(config))
}
