package com.softwaremill.bootzooka.infrastructure

import java.net.URI
import cats.effect.{IO, Resource}
import com.typesafe.scalalogging.StrictLogging
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.duration._
import Doobie._
import com.softwaremill.bootzooka.config.Sensitive
import com.softwaremill.macwire.autocats.autowire

import scala.concurrent.ExecutionContext

/** Configures the database, setting up the connection pool and performing migrations. */
class DB(_config: DBConfig) extends StrictLogging {

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

  val transactorResource: Resource[IO, Transactor[IO]] = {
    /*
     * When running DB operations, there are three thread pools at play:
     * (1) connectEC: this is a thread pool for awaiting connections to the database. There might be an arbitrary
     * number of clients waiting for a connection, so this should be bounded.
     *
     * See also: https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html#about-threading
     */
    def buildTransactor(ec: ExecutionContext) = HikariTransactor.newHikariTransactor[IO](
      config.driver,
      config.url,
      config.username,
      config.password.value,
      ec
    )
    autowire[Transactor[IO]](
      doobie.util.ExecutionContexts.fixedThreadPool[IO](config.connectThreadPoolSize),
      buildTransactor _
    ).evalTap(connectAndMigrate)
  }

  private def connectAndMigrate(xa: Transactor[IO]): IO[Unit] = {
    (migrate() >> testConnection(xa) >> IO(logger.info("Database migration & connection test complete"))).onError { e: Throwable =>
      logger.warn("Database not available, waiting 5 seconds to retry...", e)
      IO.sleep(5.seconds) >> connectAndMigrate(xa)
    }
  }

  private val flyway = {
    Flyway
      .configure()
      .dataSource(config.url, config.username, config.password.value)
      .load()
  }

  private def migrate(): IO[Unit] = {
    if (config.migrateOnStart) {
      IO(flyway.migrate()).void
    } else IO.unit
  }

  private def testConnection(xa: Transactor[IO]): IO[Unit] =
    IO {
      sql"select 1".query[Int].unique.transact(xa)
    }.void
}
