package com.softwaremill.bootzooka.infrastructure

import java.net.URI
import org.flywaydb.core.Flyway

import scala.concurrent.duration.*
import Magnum.*
import com.augustnagro.magnum.{SqlLogger, Transactor, connect}
import com.softwaremill.bootzooka.config.Sensitive
import com.softwaremill.bootzooka.infrastructure.DB.LeftException
import com.softwaremill.bootzooka.logging.Logging
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import ox.{IO, discard, sleep}

import java.io.Closeable
import javax.sql.DataSource
import scala.annotation.tailrec
import scala.util.NotGiven
import scala.util.control.{NoStackTrace, NonFatal}

class DB(dataSource: DataSource & Closeable) extends Logging with AutoCloseable:
  private val transactor = Transactor(
    dataSource = dataSource,
    sqlLogger = SqlLogger.logSlowQueries(200.millis)
  )

  /** Runs `f` in a transaction. The transaction is commited if the result is a [[Right]], and rolled back otherwise. */
  def transactEither[E, T](f: DbTx ?=> Either[E, T])(using IO): Either[E, T] =
    try com.augustnagro.magnum.transact(transactor)(Right(f.fold(e => throw LeftException(e), identity)))
    catch case e: LeftException[E] => Left(e.left)

  /** Runs `f` in a transaction. The result cannot be an `Either`, as then [[transactEither]] should be used. The transaction is commited if
    * no exception is thrown.
    */
  def transact[T](f: DbTx ?=> T)(using NotGiven[T <:< Either[_, _]], IO): T =
    com.augustnagro.magnum.transact(transactor)(f)

  override def close(): Unit = IO.unsafe(dataSource.close())

object DB extends Logging:
  private class LeftException[E](val left: E) extends RuntimeException with NoStackTrace

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
        case NonFatal(e) =>
          logger.warn("Database not available, waiting 5 seconds to retry...", e)
          sleep(5.seconds)
          connectAndMigrate(ds)

    val ds = new HikariDataSource(hikariConfig)
    connectAndMigrate(ds)
    DB(ds)
