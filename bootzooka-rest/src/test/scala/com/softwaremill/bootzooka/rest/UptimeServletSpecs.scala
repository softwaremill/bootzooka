package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.BootstrapServletSpec

class UptimeServletSpecs extends BootstrapServletSpec {
  addServlet(new MockedUptimeServlet(), "/*")

  "GET /" should "return status 200" in {
    get("/") {
      status should be (200)
    }
  }

  "GET /" should "return JSON content type" in {
    get("/") {
      header.get("Content-Type").get should include ("application/json")
    }
  }

  "GET /" should "container value 10 in body" in {
    get("/") {
      body should include ("{\"value\":10}")
    }
  }

  class MockedUptimeServlet extends UptimeServlet {
    override def serverUptime() = {
      10
    }
  }
}
