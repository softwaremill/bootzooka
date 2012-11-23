package pl.softwaremill.bootstrap.rest

import org.scalatra.ScalatraServlet
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s.{DefaultFormats, Formats}

class JsonServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult {

    protected implicit val jsonFormats: Formats = DefaultFormats

    before() {
      contentType = formats("json")
    }

}