package pl.softwaremill.bootstrap.rest

import org.scalatra.ScalatraServlet
import java.util.Date

class UptimeServlet extends ScalatraServlet with JsonHelpers {

  val serverStartDate = new Date()


  get("/") {
    Json((new Date().getTime - serverStartDate.getTime) / 1000)
  }

}
