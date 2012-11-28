package pl.softwaremill.bootstrap

import org.scalatra.test.specs2.ScalatraSpec
import org.specs2.mock.Mockito
import org.specs2.matcher.ThrownExpectations

trait BootstrapServletSpec extends ScalatraSpec with Mockito with ThrownExpectations {

  val defaultJsonHeaders = Map("Content-Type" -> "application/json;charset=UTF-8")

}
