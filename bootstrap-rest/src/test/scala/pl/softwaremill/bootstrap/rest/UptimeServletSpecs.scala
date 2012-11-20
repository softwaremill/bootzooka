package pl.softwaremill.bootstrap.rest

import org.scalatra.test.ScalatraTests
import org.specs2.matcher.ShouldMatchers
import org.scalatest.FunSuite
import org.scalatest.concurrent.Eventually

class UptimeServletSpecs extends FunSuite with ShouldMatchers with ScalatraTests {

  addServlet(classOf[UptimeServlet], "/*")

  test("GET / must return status Ok, JSON and uptime") {
    get("/") {
      status should equalTo(200)
      header.get("Content-Type").get should contain("application/json")
      body should contain("{\"value\":3}")
    }
  }

}
