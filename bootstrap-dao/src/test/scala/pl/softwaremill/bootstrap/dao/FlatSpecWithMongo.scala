package pl.softwaremill.bootstrap.dao

import com.mongodb.casbah.{MongoDB, MongoConnection}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec}

trait FlatSpecWithMongo extends FlatSpec with BeforeAndAfterAll {
  val mongoPort = 24567

  protected var mongoRunner: MongoRunner = null
  protected implicit var mongoConn: MongoDB = null

  override protected def beforeAll() {
    super.beforeAll()
    startMongo()
  }

  override protected def afterAll() {
    stopMongo()
    super.afterAll()
  }

  def startMongo() {
    mongoRunner = MongoRunner.run(mongoPort, verbose = true)
    mongoConn = MongoConnection("localhost", mongoPort)("bootstrap")
  }

  def stopMongo() {
    mongoRunner.stop()
  }
}
