package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import org.scalatest._

import scala.slick.jdbc.StaticQuery

trait FlatSpecWithSql extends FlatSpec with BeforeAndAfterAll with Matchers {

  private val connectionString = "jdbc:h2:mem:bootzooka_test" + this.getClass.getSimpleName + ";DB_CLOSE_DELAY=-1"

  val sqlDatabase = SqlDatabase.createEmbedded(connectionString)

  override protected def beforeAll() {
    super.beforeAll()
    createAll()
  }

  def clearData() {
    dropAll()
    createAll()
  }

  override protected def afterAll() {
    super.afterAll()
    dropAll()
    sqlDatabase.close()
  }

  private def dropAll() {
    sqlDatabase.db.withSession { implicit session =>
      StaticQuery.updateNA("DROP ALL OBJECTS").execute
    }
  }

  private def createAll() {
    sqlDatabase.updateSchema()
  }
}
