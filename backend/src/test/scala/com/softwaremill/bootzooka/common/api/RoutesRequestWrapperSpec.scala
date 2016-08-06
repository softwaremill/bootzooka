package com.softwaremill.bootzooka.common.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.softwaremill.bootzooka.common.api.`X-Content-Type-Options`.`nosniff`
import com.softwaremill.bootzooka.common.api.`X-Frame-Options`.`DENY`
import com.softwaremill.bootzooka.common.api.`X-XSS-Protection`.`1; mode=block`
import org.scalatest.{FlatSpec, Matchers}

class RoutesRequestWrapperSpec extends FlatSpec with Matchers with ScalatestRouteTest {

  it should "return a response wirth security headers" in {
    val routes = new RoutesRequestWrapper {}.requestWrapper {
      get {
        complete("ok")
      }
    }

    Get() ~> routes ~> check {
      response.header[`X-Frame-Options`].get shouldBe `X-Frame-Options`(`DENY`)
      response.header[`X-Content-Type-Options`].get shouldBe `X-Content-Type-Options`(`nosniff`)
      response.header[`X-XSS-Protection`].get shouldBe `X-XSS-Protection`(`1; mode=block`)
    }
  }
}
