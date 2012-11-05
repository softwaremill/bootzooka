package pl.softwaremill.bootstrap.rest

import org.scalatra.ScalatraServlet
import java.util.Date
import org.scalatra.json.{JacksonJsonSupport, JValueResult}
import org.json4s.{Formats, DefaultFormats}

class UptimeServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult {

  val serverStartDate = new Date()

  protected implicit val jsonFormats: Formats = DefaultFormats


  before() {
    contentType = formats("json")
  }

  get("/") {
    (new Date().getTime - serverStartDate.getTime) / 1000
  }

}
