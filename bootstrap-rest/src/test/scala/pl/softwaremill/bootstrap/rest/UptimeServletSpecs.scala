package pl.softwaremill.bootstrap.rest

import org.scalatra.test.specs2.ScalatraSpec
import org.joda.time.DateTime

class UptimeServletSpecs extends ScalatraSpec {

  def is = "GET / on UptimServlet"    ^
    `should return status 200`        ^
    `should return JSON content type` ^
    `bust must contain value 10`
  end

  addServlet(new UptimeServlet(new DateTime().minusSeconds(9)), "/*")

  def `should return status 200` = get("/") {
    status should equalTo(200)
  }

  def `should return JSON content type` = get("/") {
    header.get("Content-Type").get should contain("application/json")
  }

  def `bust must contain value 10` = get("/") {
    body should contain("{\"value\":10}")
  }

}
