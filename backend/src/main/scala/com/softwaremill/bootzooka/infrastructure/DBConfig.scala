package com.softwaremill.bootzooka.infrastructure

import com.softwaremill.bootzooka.config.Sensitive

case class DBConfig(username: String, password: Sensitive, url: String, migrateOnStart: Boolean, driver: String, connectThreadPoolSize: Int)
