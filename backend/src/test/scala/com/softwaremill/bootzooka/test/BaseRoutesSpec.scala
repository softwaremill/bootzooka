package com.softwaremill.bootzooka.test

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.softwaremill.bootzooka.api.JsonSupport
import com.softwaremill.bootzooka.config.CoreConfig
import com.softwaremill.bootzooka.email.{DummyEmailService, EmailTemplatingEngine}
import com.softwaremill.bootzooka.user.{Session, UserDao, UserService}
import com.softwaremill.session.{SessionConfig, SessionManager}
import com.typesafe.config.ConfigFactory
import org.scalatest.Matchers

trait BaseRoutesSpec extends FlatSpecWithDb with ScalatestRouteTest with Matchers with JsonSupport { spec =>

  lazy val sessionConfig = SessionConfig.fromConfig(ConfigFactory.load()).copy(sessionEncryptData = true)

  implicit def mapCbs = CanBeSerialized[Map[String, String]]

  val emailService = new DummyEmailService()
  val emailTemplatingEngine = new EmailTemplatingEngine
  val userDao = new UserDao(sqlDatabase)
  val userService = new UserService(userDao, emailService, emailTemplatingEngine)
  val config = new CoreConfig {
    override def rootConfig = ConfigFactory.load()
  }

  trait TestRoutesSupport {
    lazy val sessionConfig = spec.sessionConfig
    implicit def materializer = spec.materializer
    implicit def ec = spec.executor
    implicit def sessionManager = new SessionManager[Session](sessionConfig)
    implicit def refreshTokenStorage = null
  }
}
