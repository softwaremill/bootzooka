package com.softwaremill.bootzooka.test

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.implicits.global
import com.softwaremill.bootzooka.infrastructure.{CorrelationId, DBConfig}
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.typesafe.scalalogging.StrictLogging
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.annotation.tailrec
import scala.concurrent.duration._

/** A work-around to use the `xaResource` imperatively.
  */
class TestDB(config: DBConfig) extends StrictLogging {

  var xa: Transactor[IO] = _
  private val xaReady: Queue[IO, Transactor[IO]] = Queue.unbounded[IO, Transactor[IO]].unsafeRunSync()
  private val done: Queue[IO, Unit] = Queue.unbounded[IO, Unit].unsafeRunSync()

  {
    val xaResource = for {
      connectEC <- doobie.util.ExecutionContexts.fixedThreadPool[IO](config.connectThreadPoolSize)
      xa <- HikariTransactor.newHikariTransactor[IO](
        config.driver,
        config.url,
        config.username,
        config.password.value,
        connectEC
      )
    } yield CorrelationId.correlationIdTransactor(xa)

    // first extracting it from the use method, then stopping when the `done` mvar is filled (when `close()` is invoked)
    xaResource
      .use { _xa =>
        xaReady.offer(_xa) >> done.take
      }
      .start
      .unsafeRunSync()

    xa = xaReady.take.unsafeRunSync()
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
    sql"select 1".query[Int].unique.transact(xa).unsafeRunTimed(1.minute)
    ()
  }

  def close(): Unit = {
    done.offer(()).unsafeRunTimed(1.minute)
  }
}
