package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.config.ConfigModule
import com.softwaremill.bootzooka.infrastructure.DB

/**
  * Initialised resources needed by the application to start.
  */
trait InitModule extends ConfigModule {
  lazy val db: DB = new DB(config.db)
}
