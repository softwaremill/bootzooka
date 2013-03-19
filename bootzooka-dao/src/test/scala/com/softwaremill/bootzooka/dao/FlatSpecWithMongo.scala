package com.softwaremill.bootzooka.dao

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import net.liftweb.mongodb.{MongoDB, DefaultMongoIdentifier}
import com.mongodb.Mongo

trait FlatSpecWithMongo extends FlatSpec with BeforeAndAfterAll {
  val mongoPort = 24567

  protected var mongoRunner: MongoRunner = null

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
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo("localhost", mongoPort), "bootstrap_test")
  }

  def stopMongo() {
    mongoRunner.stop()
  }
}
