package com.softwaremill.bootzooka.test

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.softwaremill.bootzooka.config.Sensitive
import com.softwaremill.bootzooka.infrastructure.{DB, DBConfig}
import com.softwaremill.bootzooka.logging.Logging
import org.flywaydb.core.Flyway
import org.postgresql.jdbc.PgConnection
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import ox.discard

import scala.compiletime.uninitialized

/** Base trait for tests which use the database. The database is cleaned after each test. */
trait TestEmbeddedPostgres extends BeforeAndAfterEach with BeforeAndAfterAll with Logging { self: Suite =>
  private var postgres: EmbeddedPostgres = uninitialized
  private var currentDbConfig: DBConfig = uninitialized
  var currentDb: DB = uninitialized

  //

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    postgres = EmbeddedPostgres.builder().start()
    val url = postgres.getJdbcUrl("postgres")
    postgres.getPostgresDatabase.getConnection.asInstanceOf[PgConnection].setPrepareThreshold(100)
    currentDbConfig = TestConfig.db.copy(
      username = "postgres",
      password = Sensitive(""),
      url = url,
      migrateOnStart = true
    )
    currentDb = DB.createTestMigrate(currentDbConfig)
  }

  override protected def afterAll(): Unit =
    currentDb.close()
    postgres.close()
    super.afterAll()

  //

  override protected def beforeEach(): Unit =
    super.beforeEach()
    flyway().migrate().discard

  override protected def afterEach(): Unit =
    clean()
    super.afterEach()

  private def clean(): Unit = flyway().clean().discard

  private def flyway(): Flyway = Flyway
    .configure()
    .dataSource(currentDbConfig.url, currentDbConfig.username, currentDbConfig.password.value)
    .cleanDisabled(false)
    .load()
}
