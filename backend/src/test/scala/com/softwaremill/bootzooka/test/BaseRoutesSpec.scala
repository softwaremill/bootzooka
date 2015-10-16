package com.softwaremill.bootzooka.test

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.softwaremill.bootzooka.api.JsonSupport
import com.softwaremill.bootzooka.user.Session
import com.softwaremill.session.{SessionManager, SessionConfig}
import com.typesafe.config.ConfigFactory
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

trait BaseRoutesSpec extends FlatSpec with ScalatestRouteTest with Matchers with MockitoSugar with JsonSupport { spec =>

  lazy val sessionConfig = SessionConfig.fromConfig(ConfigFactory.load()).withClientSessionEncryptData(true)

  implicit def mapCbs = CanBeSerialized[Map[String, String]]

  trait TestRoutesSupport {
    lazy val sessionConfig = spec.sessionConfig
    implicit def materializer = spec.materializer
    implicit def ec = spec.executor
    implicit def sessionManager = new SessionManager[Session](sessionConfig)
    implicit def rememberMeStorage = null
  }
}
