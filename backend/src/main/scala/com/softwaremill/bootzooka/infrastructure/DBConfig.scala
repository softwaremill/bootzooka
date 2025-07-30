package com.softwaremill.bootzooka.infrastructure

import com.softwaremill.bootzooka.config.Sensitive
import pureconfig.ConfigReader

case class DBConfig(username: String, password: Sensitive, url: String, migrateOnStart: Boolean, driver: String) derives ConfigReader
