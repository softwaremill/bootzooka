package com.softwaremill.bootzooka.test

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.softwaremill.bootzooka.common.api.JsonSupport
import com.softwaremill.bootzooka.user.application.Session
import com.softwaremill.session.{SessionConfig, SessionManager}
import com.typesafe.config.ConfigFactory
import org.scalatest.Matchers
import scala.concurrent.duration._

trait BaseRoutesSpec extends FlatSpecWithDb with ScalatestRouteTest with Matchers with JsonSupport { spec =>

  lazy val sessionConfig = SessionConfig.fromConfig(ConfigFactory.load()).copy(sessionEncryptData = true)

  implicit def mapCbs = CanBeSerialized[Map[String, String]]

  implicit val timeout = RouteTestTimeout(10 second span)

  trait TestRoutesSupport {
    lazy val sessionConfig = spec.sessionConfig
    implicit def materializer = spec.materializer
    implicit def ec = spec.executor
    implicit def sessionManager = new SessionManager[Session](sessionConfig)
    implicit def refreshTokenStorage = null
  }
}
