package pl.softwaremill.bootstrap.rest

import org.scalatra.ScalatraServlet
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s.{DefaultFormats, Formats}
import pl.softwaremill.bootstrap.auth.RememberMeSupport

abstract class JsonServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult with RememberMeSupport {

    protected implicit val jsonFormats: Formats = DefaultFormats

    before() {
      contentType = formats("json")
    }

}