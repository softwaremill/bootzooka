package com.softwaremill.bootzooka.test

import cats.effect.{Blocker, ContextShift}
import com.softwaremill.bootzooka.infrastructure.DBConfig
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.typesafe.scalalogging.StrictLogging
import doobie.hikari.HikariTransactor
import monix.catnap.MVar
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.flywaydb.core.Flyway

import scala.annotation.tailrec
import scala.concurrent.duration._

/**
  * A work-around to use the `xaResource` imperatively.
  */
class TestDB(config: DBConfig) extends StrictLogging {

  var xa: Transactor[Task] = _
  private val xaReady: MVar[Task, Transactor[Task]] = MVar.empty[Task, Transactor[Task]]().runSyncUnsafe()
  private val done: MVar[Task, Unit] = MVar.empty[Task, Unit]().runSyncUnsafe()

  {
    implicit val contextShift: ContextShift[Task] = Task.contextShift(monix.execution.Scheduler.global)

    val xaResource = for {
      connectEC <- doobie.util.ExecutionContexts.fixedThreadPool[Task](config.connectThreadPoolSize)
      transactEC <- doobie.util.ExecutionContexts.cachedThreadPool[Task]
      xa <- HikariTransactor.newHikariTransactor[Task](
        config.driver,
        config.url,
        config.username,
        config.password.value,
        connectEC,
        Blocker.liftExecutionContext(transactEC)
      )
    } yield xa

    // first extracting it from the use method, then stopping when the `done` mvar is filled (when `close()` is invoked)
    xaResource
      .use { _xa =>
        xaReady.put(_xa) >> done.take
      }
      .startAndForget
      .runSyncUnsafe()

    xa = xaReady.take.runSyncUnsafe()
  }

  private val flyway = {
    Flyway
      .configure()
      .dataSource(config.url, config.username, config.password.value)
      .load()
  }

  @tailrec
  final def connectAndMigrate(): Unit = {
    try {
      migrate()
      testConnection()
      logger.info("Database migration & connection test complete")
    } catch {
      case e: Exception =>
        logger.warn("Database not available, waiting 5 seconds to retry...", e)
        Thread.sleep(5000)
        connectAndMigrate()
    }
  }

  def migrate(): Unit = {
    if (config.migrateOnStart) {
      flyway.migrate()
      ()
    }
  }

  def clean(): Unit = {
    flyway.clean()
  }

  def testConnection(): Unit = {
    sql"select 1".query[Int].unique.transact(xa).runSyncUnsafe(1.minute)
    ()
  }

  def close(): Unit = {
    done.put(()).runSyncUnsafe(1.minute)
  }
}
