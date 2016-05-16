package com.softwaremill.bootzooka.test

import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.softwaremill.bootzooka.utils.ActorPerRequestFactory
import com.softwaremill.bootzooka.utils.http.PerRequest._
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class BaseActorPerRequestSpec extends TestKit(ActorSystem("main")) with FlatSpecLike with SpecWithDb
    with DefaultTimeout with ImplicitSender with Matchers with BeforeAndAfterAll {
  spec =>

  override def afterAll {
    super.afterAll()
    shutdown()
  }

  implicit class ActorFactory(factory: ActorPerRequestFactory) {

    def whenSend(cmd: Command) = {
      (system actorOf factory.props) ! cmd
      this
    }

    def thenExpect(msg: Event) = {
      within(2 seconds) {
        spec expectMsg msg
      }
      this
    }

  }
}
