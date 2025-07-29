package com.softwaremill.bootzooka.infrastructure

import com.augustnagro.magnum.{DbCodec, DbTx, SqlLogger, Transactor, connect, sql}
import com.softwaremill.bootzooka.infrastructure.DB.LeftException
import com.softwaremill.bootzooka.logging.Logging
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.flywaydb.core.Flyway
import ox.*

import java.io.Closeable
import javax.sql.DataSource
import scala.annotation.tailrec
import scala.concurrent.duration.*
import scala.util.NotGiven
import scala.util.control.{NoStackTrace, NonFatal}

class DB(dataSource: DataSource & Closeable) extends Logging with AutoCloseable:
  private val transactor = Transactor(
    dataSource = dataSource,
    sqlLogger = SqlLogger.logSlowQueries(200.millis)
  )

  /** Runs `f` in a transaction. The transaction is commited if the result is a [[Right]], and rolled back otherwise. */
  def transactEither[E, T](f: DbTx ?=> Either[E, T]): Either[E, T] =
    try com.augustnagro.magnum.transact(transactor)(Right(f.fold(e => throw LeftException(e), identity)))
    catch case e: LeftException[E] @unchecked => Left(e.left)

  /** Runs `f` in a transaction. The result cannot be an `Either`, as then [[transactEither]] should be used. The transaction is commited if
    * no exception is thrown.
    */
  def transact[T](f: DbTx ?=> T)(using NotGiven[T <:< Either[?, ?]]): T =
    com.augustnagro.magnum.transact(transactor)(f)

  override def close(): Unit = dataSource.close()
end DB

object DB extends Logging:
  private class LeftException[E](val left: E) extends RuntimeException with NoStackTrace

  /** Configures the database, setting up the connection pool and performing migrations. */
  def createTestMigrate(config: DBConfig): DB =
    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(config.url)
    hikariConfig.setUsername(config.username)
    hikariConfig.setPassword(config.password.value)
    hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    hikariConfig.setConnectionTestQuery("SELECT 1")
    hikariConfig.addHealthCheckProperty("connectivityCheckTimeoutMs", "1000")
    hikariConfig.setThreadFactory(Thread.ofVirtual().factory())

    val flyway = Flyway
      .configure()
      .dataSource(config.url, config.username, config.password.value)
      .load()

    def migrate(): Unit = if config.migrateOnStart then flyway.migrate().discard
    def testConnection(ds: DataSource): Unit = connect(ds)(sql"SELECT 1".query[Int].run()).discard

    @tailrec
    def connectAndMigrate(ds: DataSource): Unit =
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
  end createTestMigrate
end DB
