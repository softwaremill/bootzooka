package com.softwaremill.bootzooka.infrastructure

case class DBConfig(username: String, password: String, url: String, migrateOnStart: Boolean, driver: String, connectThreadPoolSize: Int) {
  override def toString: String = s"DBConfig($username,***,$url,$migrateOnStart,$driver,$connectThreadPoolSize)"
}
