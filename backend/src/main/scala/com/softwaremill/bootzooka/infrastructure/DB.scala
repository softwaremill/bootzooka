package com.softwaremill.bootzooka.infrastructure

import cats.effect.{ContextShift, Resource}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import doobie.hikari.HikariTransactor
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.flywaydb.core.Flyway

import scala.concurrent.duration._
import Doobie._

class DB(config: DBConfig) extends StrictLogging {

  private val flyway = {
    Flyway
      .configure()
      .dataSource(config.url, config.username, config.password)
      .placeholderPrefix("$%{") // so it won't interfere with email templates placeholders
      .load()
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
    (migrate() >> testConnection(xa)).onErrorRecoverWith {
      case e: Exception =>
        logger.warn("Database not available, waiting 5 seconds to retry...", e)
        Task.sleep(5.seconds) >> connectAndMigrate(xa)
    }
  }

  private def migrate(): Task[Unit] = {
    if (config.migrateOnStart) {
      Task(flyway.migrate()).void
    } else Task.unit
  }

  private def testConnection(xa: Transactor[Task]): Task[Unit] = Task {
    sql"select 1".query[Int].unique.transact(xa)
  }.void
}
