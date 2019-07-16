package com.softwaremill.bootzooka.infrastructure

import java.net.URI

import cats.effect.{ContextShift, Resource}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import doobie.hikari.HikariTransactor
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.flywaydb.core.Flyway

import scala.concurrent.duration._
import Doobie._

/**
  * Configures the database, setting up the connection pool and performing migrations.
  */
class DB(_config: DBConfig) extends StrictLogging {

  private val config: DBConfig = {
    // on heroku, the url is passed without the jdbc: prefix, and with a different scheme
    // see https://devcenter.heroku.com/articles/connecting-to-relational-databases-on-heroku-with-java#using-the-database_url-in-plain-jdbc
    if (_config.url.startsWith("postgres://")) {
      val dbUri = URI.create(_config.url)
      val usernamePassword = dbUri.getUserInfo.split(":")
      _config.copy(
        username = usernamePassword(0),
        password = if (usernamePassword.length > 1) usernamePassword(1) else "",
        url = "jdbc:postgresql://" + dbUri.getHost + ':' + dbUri.getPort + dbUri.getPath
      )
    } else _config
  }

  val transactorResource: Resource[Task, Transactor[Task]] = {
    implicit val contextShift: ContextShift[Task] = Task.contextShift(global)

    for {
      connectEC <- doobie.util.ExecutionContexts.fixedThreadPool[Task](config.connectThreadPoolSize)
      transactEC <- doobie.util.ExecutionContexts.cachedThreadPool[Task]
      xa <- HikariTransactor.newHikariTransactor[Task](
        config.driver,
        config.url,
        config.username,
        config.password,
        connectEC,
        transactEC
      )
      _ <- Resource.liftF(connectAndMigrate(xa))
    } yield xa
  }

  private def connectAndMigrate(xa: Transactor[Task]): Task[Unit] = {
    (migrate() >> testConnection(xa) >> Task(logger.info("Database migration & connection test complete"))).onErrorRecoverWith {
      case e: Exception =>
        logger.warn("Database not available, waiting 5 seconds to retry...", e)
        Task.sleep(5.seconds) >> connectAndMigrate(xa)
    }
  }

  private val flyway = {
    Flyway
      .configure()
      .dataSource(config.url, config.username, config.password)
      .load()
  }

  private def migrate(): Task[Unit] = {
    if (config.migrateOnStart) {
      Task(flyway.migrate()).void
    } else Task.unit
  }

  private def testConnection(xa: Transactor[Task]): Task[Unit] =
    Task {
      sql"select 1".query[Int].unique.transact(xa)
    }.void
}
