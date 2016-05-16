package com.softwaremill.bootzooka.test

import akka.actor.ActorSystem
import org.scalatest._

trait SpecWithActorSystem extends BeforeAndAfterAll {
  self: Suite =>

  val actorSystem = ActorSystem("main")

  override protected def afterAll() {
    super.afterAll()
    actorSystem.terminate()
  }
}
