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
import scala.reflect.ClassTag
import scala.util.NotGiven

class DB(dataSource: DataSource & Closeable) extends Logging with AutoCloseable:
  def transactEither[E <: Exception: ClassTag, T](f: DbTx ?=> Either[E, T])(using IO): Either[E, T] =
    try
      com.augustnagro.magnum.transact(dataSource) {
        Right(f.fold(throw _, identity))
      }
    catch case e: E if summon[ClassTag[E]].runtimeClass.isAssignableFrom(e.getClass) => Left(e)

  // TODO: test & document
  def transact[T](f: DbTx ?=> T)(using NotGiven[T <:< Either[_, _]], IO): T =
    com.augustnagro.magnum.transact(dataSource)(f)

  override def close(): Unit = IO.unsafe(dataSource.close())

object DB extends Logging:
  /** Configures the database, setting up the connection pool and performing migrations. */
  def createTestMigrate(_config: DBConfig)(using IO): DB =
    val config: DBConfig =
      if (_config.url.startsWith("postgres://")) {
        val dbUri = URI.create(_config.url)
        val usernamePassword = dbUri.getUserInfo.split(":")
        _config.copy(
          username = usernamePassword(0),
          password = Sensitive(if (usernamePassword.length > 1) usernamePassword(1) else ""),
          url = "jdbc:postgresql://" + dbUri.getHost + ':' + dbUri.getPort + dbUri.getPath
        )
      } else _config
    end config

    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(_config.url)
    hikariConfig.setUsername(config.username)
    hikariConfig.setPassword(config.password.value)
    hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    hikariConfig.setThreadFactory(Thread.ofVirtual().factory())

    val flyway = Flyway
      .configure()
      .dataSource(config.url, config.username, config.password.value)
      .load()

    def migrate()(using IO): Unit = if config.migrateOnStart then flyway.migrate().discard
    def testConnection(ds: DataSource): Unit = connect(ds)(sql"SELECT 1".query[Int].run()).discard

    @tailrec
    def connectAndMigrate(ds: DataSource)(using IO): Unit =
      try
        migrate()
        testConnection(ds)
        logger.info("Database migration & connection test complete")
      catch
        case e: Exception =>
          logger.warn("Database not available, waiting 5 seconds to retry...", e)
          sleep(5.seconds)
          connectAndMigrate(ds)

    val ds = new HikariDataSource(hikariConfig)
    connectAndMigrate(ds)
    DB(ds)
