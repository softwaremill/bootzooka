package com.softwaremill.bootzooka.test

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.softwaremill.bootzooka.api.{JsonSupport, Session}
import com.softwaremill.session.{SessionManager, SessionConfig}
import com.typesafe.config.ConfigFactory
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

trait BaseRoutesSpec extends FlatSpec with ScalatestRouteTest with Matchers with MockitoSugar with JsonSupport { spec =>

  implicit def mapCbs = CanBeSerialized[Map[String, String]]

  def stringToJson(string: String): JValue = {
    parse(string)
  }

  def valueFromWrapper(w: JValue) = (w \ "value").extract[String]

  lazy val sessionConfig = SessionConfig.fromConfig(ConfigFactory.load()).withClientSessionEncryptData(true)

  trait TestRoutesSupport {
    lazy val sessionConfig = spec.sessionConfig
    implicit def materializer = spec.materializer
    implicit def ec = spec.executor
    implicit def sessionManager = new SessionManager[Session](sessionConfig)
    implicit def rememberMeStorage = null
  }
}
