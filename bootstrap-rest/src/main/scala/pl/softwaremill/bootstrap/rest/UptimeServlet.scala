package pl.softwaremill.bootstrap.rest

import org.scalatra.ScalatraServlet
import org.scalatra.json.{JacksonJsonSupport, JValueResult}
import org.json4s.{Formats, DefaultFormats}
import pl.softwaremill.bootstrap.common.JsonWrapper
import org.joda.time.{Duration, DateTime}

class UptimeServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult {

  val serverStartDate = new DateTime()

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  get("/") {
    JsonWrapper(new Duration(serverStartDate, new DateTime()).getStandardSeconds)
  }

}
