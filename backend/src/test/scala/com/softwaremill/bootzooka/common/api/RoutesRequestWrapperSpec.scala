/*
 * COPYRIGHT (c) 2016 VOCADO, LLC.  ALL RIGHTS RESERVED.  THIS SOFTWARE CONTAINS
 * TRADE SECRETS AND/OR CONFIDENTIAL INFORMATION PROPRIETARY TO VOCADO, LLC AND/OR
 * ITS LICENSORS. ACCESS TO AND USE OF THIS INFORMATION IS STRICTLY LIMITED AND
 * CONTROLLED BY VOCADO, LLC.  THIS SOFTWARE MAY NOT BE COPIED, MODIFIED, DISTRIBUTED,
 * DISPLAYED, DISCLOSED OR USED IN ANY WAY NOT EXPRESSLY AUTHORIZED BY VOCADO, LLC IN WRITING.
 */

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
