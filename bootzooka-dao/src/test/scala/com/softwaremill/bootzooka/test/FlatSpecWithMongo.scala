package com.softwaremill.bootzooka.test

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import net.liftweb.mongodb.{MongoDB, DefaultMongoIdentifier}
import com.github.fakemongo.Fongo

trait FlatSpecWithMongo extends FlatSpec with BeforeAndAfterAll {

  val fongo = new Fongo("mongo test server 1")

  override protected def beforeAll() {
    super.beforeAll()
    startMongo()
  }

  def startMongo() {
    MongoDB.defineDb(DefaultMongoIdentifier, fongo.getMongo, "bootzooka_test")
  }
}
