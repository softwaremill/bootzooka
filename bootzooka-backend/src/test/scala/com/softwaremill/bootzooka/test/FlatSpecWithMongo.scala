package com.softwaremill.bootzooka.test

import net.liftweb.util.DefaultConnectionIdentifier
import org.scalatest.{Matchers, BeforeAndAfterAll, FlatSpec}
import net.liftweb.mongodb.MongoDB
import com.github.fakemongo.Fongo

trait FlatSpecWithMongo extends FlatSpec with BeforeAndAfterAll with Matchers {

  val fongo = new Fongo("mongo test server 1")

  override protected def beforeAll() {
    super.beforeAll()
    startMongo()
  }

  def startMongo() {
    MongoDB.defineDb(DefaultConnectionIdentifier, fongo.getMongo, "bootzooka_test")
  }
}
