package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.common.sql.SqlDatabase
import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

trait FlatSpecWithDb extends FlatSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with ScalaFutures
    with IntegrationPatience {

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
    import sqlDatabase.driver.api._
    sqlDatabase.db.run(sqlu"DROP ALL OBJECTS").futureValue
  }

  private def createAll() {
    sqlDatabase.updateSchema()
  }

  override protected def afterEach() {
    try {
      clearData()
    }
    catch {
      case e: Exception => e.printStackTrace()
    }

    super.afterEach()
  }
}
