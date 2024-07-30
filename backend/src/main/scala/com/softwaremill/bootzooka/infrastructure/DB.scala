package com.softwaremill.bootzooka.infrastructure

import java.net.URI
import org.flywaydb.core.Flyway

import scala.concurrent.duration.*
import Magnum.*
import com.augustnagro.magnum.connect
import com.softwaremill.bootzooka.config.Sensitive
import com.softwaremill.bootzooka.logging.Logging
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import ox.{IO, discard, sleep}

import java.io.Closeable
import javax.sql.DataSource
import scala.annotation.tailrec

/** Configures the database, setting up the connection pool and performing migrations. */
class DB(_config: DBConfig) extends Logging:

  private val config: DBConfig = {
    if (_config.url.startsWith("postgres://")) {
      val dbUri = URI.create(_config.url)
      val usernamePassword = dbUri.getUserInfo.split(":")
      _config.copy(
        username = usernamePassword(0),
        password = Sensitive(if (usernamePassword.length > 1) usernamePassword(1) else ""),
        url = "jdbc:postgresql://" + dbUri.getHost + ':' + dbUri.getPort + dbUri.getPath
      )
    } else _config
  }

  private val hikariConfig = new HikariConfig()
  hikariConfig.setJdbcUrl(_config.url)
  hikariConfig.setUsername(config.username)
  hikariConfig.setPassword(config.password.value)
  hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
  hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
  hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
  hikariConfig.setThreadFactory(Thread.ofVirtual().factory())

  @tailrec
  private def connectAndMigrate(ds: DataSource)(using IO): Unit =
    try
      migrate()
      testConnection(ds)
      logger.info("Database migration & connection test complete")
    catch
      case e: Exception =>
        logger.warn("Database not available, waiting 5 seconds to retry...", e)
        sleep(5.seconds)
        connectAndMigrate(ds)

  private val flyway = Flyway
    .configure()
    .dataSource(config.url, config.username, config.password.value)
    .load()

  private def migrate()(using IO): Unit = if config.migrateOnStart then flyway.migrate().discard
  private def testConnection(ds: DataSource): Unit = connect(ds)(sql"SELECT 1".query[Int].run()).discard

  val ds: DataSource & Closeable =
    val temp = new HikariDataSource(hikariConfig)
    IO.unsafe(connectAndMigrate(temp))
    temp
