package pl.softwaremill.bootstrap.rest

import org.scalatra.ScalatraServlet
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s._
import java.io.Writer
import org.apache.commons.lang3.StringEscapeUtils._

class JsonServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult {

    protected implicit val jsonFormats: Formats = DefaultFormats

    before() {
      contentType = formats("json")
    }

  override def writeJson(json: JValue, writer: Writer) {
    (json \ "notEscapedData") match {
      case JNothing => {
        val escapedJson = json.map((x:JValue) =>
          x match {
            case JString(y) => JString(escapeHtml4(y))
            case _ => x
          }
        )
        mapper.writeValue(writer, escapedJson)
      }
      case _ => {
        mapper.writeValue(writer, json \ "notEscapedData")
      }
    }
  }

}