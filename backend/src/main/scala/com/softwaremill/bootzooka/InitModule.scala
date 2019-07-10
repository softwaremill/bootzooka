package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.infrastructure.DB

trait InitModule extends ConfigModule {
  lazy val db: DB = new DB(config.db)
}
