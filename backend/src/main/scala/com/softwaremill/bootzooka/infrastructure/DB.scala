package com.softwaremill.bootzooka.infrastructure

import cats.effect.Resource
import cats.effect.kernel.Async
import cats.implicits._
import com.softwaremill.bootzooka.config.Sensitive
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.typesafe.scalalogging.StrictLogging
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import java.net.URI
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/** Configures the database, setting up the connection pool and performing migrations. */
class DB[F[_]: Async](_config: DBConfig) extends StrictLogging {

  private val config: DBConfig = {
    // on heroku, the url is passed without the jdbc: prefix, and with a different scheme
    // see https://devcenter.heroku.com/articles/connecting-to-relational-databases-on-heroku-with-java#using-the-database_url-in-plain-jdbc
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

  val transactorResource: Resource[F, Transactor[F]] = {
    /*
     * When running DB operations, there are three thread pools at play:
     * (1) connectEC: this is a thread pool for awaiting connections to the database. There might be an arbitrary
     * number of clients waiting for a connection, so this should be bounded.
     *
     * See also: https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html#about-threading
     */
    def buildTransactor(ec: ExecutionContext) = HikariTransactor.newHikariTransactor[F](
      config.driver,
      config.url,
      config.username,
      config.password.value,
      ec
    )

    ExecutionContexts
      .fixedThreadPool[F](config.connectThreadPoolSize)
      .flatMap(buildTransactor)
      .evalTap(connectAndMigrate)
  }

  private def connectAndMigrate(xa: Transactor[F]): F[Unit] = {
    (migrate() >> testConnection(xa) >> Async[F].delay(logger.info("Database migration & connection test complete"))).onError {
      e: Throwable =>
        logger.warn("Database not available, waiting 5 seconds to retry...", e)
        Async[F].sleep(5.seconds) >> connectAndMigrate(xa)
    }
  }

  private val flyway = {
    Flyway
      .configure()
      .dataSource(config.url, config.username, config.password.value)
      .load()
  }

  private def migrate(): F[Unit] = {
    if (config.migrateOnStart) {
      Async[F].blocking {
        flyway.migrate()
      }.void
    } else Async[F].unit
  }

  private def testConnection(xa: Transactor[F]): F[Unit] =
    Async[F].blocking {
      sql"select 1".query[Int].unique.transact(xa)
    }.void
}
