package com.softwaremill.bootzooka.dao.sql

import java.io.File
import java.net.URI
import java.util.UUID
import javax.sql.DataSource

import com.mchange.v2.c3p0.{ComboPooledDataSource, DataSources}
import com.softwaremill.bootzooka.dao.DaoConfig
import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.Flyway
import org.joda.time.{DateTime, DateTimeZone}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend._

case class SqlDatabase(
    db: slick.jdbc.JdbcBackend.Database,
    driver: JdbcProfile,
    ds: DataSource
) {

  import driver.api._

  implicit val dateTimeColumnType = MappedColumnType.base[DateTime, java.sql.Timestamp](
    dt => new java.sql.Timestamp(dt.getMillis),
    t => new DateTime(t.getTime).withZone(DateTimeZone.UTC)
  )

  implicit val UUIDColumnType = MappedColumnType.base[UUID, String](
    uuid => uuid.toString,
    s => UUID.fromString(s)
  )

  def updateSchema() {
    val flyway = new Flyway()
    flyway.setDataSource(ds)
    flyway.migrate()
  }

  def close() {
    DataSources.destroy(ds)
  }
}

object SqlDatabase extends LazyLogging {

  def embeddedConnectionString(config: DaoConfig): String = {
    val fullPath = new File(config.dbH2EmbeddedDataDir, "updateimpact").getCanonicalPath
    logger.info(s"Using an embedded database, with data files located at: $fullPath")

    s"jdbc:h2:file:$fullPath"
  }

  def create(config: DaoConfig): SqlDatabase = {
    /*
    The DATABASE_URL is set by Heroku (if deploying there) and must be converted to JDBC format:
    postgres://<username>:<password>@<host>:<port>/<dbname>
    -->
    jdbc:postgresql://<host>:<port>/<dbname>?user=<username>&password=<password>
     */
    val envDatabaseUrl = System.getenv("DATABASE_URL")

    if (config.dbPostgresConnectionUrl.length > 0) {
      createPostgres(config.dbPostgresConnectionUrl, None, None)
    }
    else if (envDatabaseUrl != null) {
      val dbUri = new URI(envDatabaseUrl)
      val username = dbUri.getUserInfo.split(":")(0)
      val password = dbUri.getUserInfo.split(":")(1)
      val url = s"jdbc:postgresql://${dbUri.getHost}:${dbUri.getPort}${dbUri.getPath}"

      createPostgres(url, Some(username), Some(password))
    }
    else {
      createEmbedded(config)
    }
  }

  private def createEmbedded(config: DaoConfig): SqlDatabase = {
    createEmbedded(embeddedConnectionString(config))
  }

  def createEmbedded(connectionString: String): SqlDatabase = {
    val ds = createConnectionPool(connectionString, "org.h2.Driver", None, None)
    val db = Database.forDataSource(ds)
    SqlDatabase(db, slick.driver.H2Driver, ds)
  }

  def createPostgres(connectionString: String, username: Option[String], password: Option[String]): SqlDatabase = {
    logger.info(s"Using postgres database, connection url: $connectionString")
    val ds = createConnectionPool(connectionString, "org.postgresql.Driver", username, password)
    val db = Database.forDataSource(ds)
    SqlDatabase(db, slick.driver.PostgresDriver, ds)
  }

  private def createConnectionPool(connectionString: String, driverClass: String,
    username: Option[String], password: Option[String]) = {

    val cpds = new ComboPooledDataSource()
    cpds.setDriverClass(driverClass)
    cpds.setJdbcUrl(connectionString)
    username.foreach(cpds.setUser)
    password.foreach(cpds.setPassword)
    cpds
  }
}
