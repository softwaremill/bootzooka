package pl.softwaremill.bootstrap.rest

import org.scalatra._
import org.scalatra.json.{ JValueResult, JacksonJsonSupport }
import org.json4s._
import java.io.Writer
import org.apache.commons.lang3.StringEscapeUtils._
import org.json4s.{ DefaultFormats, Formats }
import javax.servlet.http.HttpServletResponse
import java.util.Date
import com.weiglewilczek.slf4s.Logging

class JsonServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult with Logging {

  protected implicit val jsonFormats: Formats = DefaultFormats

  val Expire = new Date().toString

  before() {
    contentType = formats("json")
    applyNoCache(response)
  }

  override def writeJson(json: JValue, writer: Writer) {
    (json \ "notEscapedData") match {
      case JNothing => {
        val escapedJson = json.map((x: JValue) =>
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

  def applyNoCache(response: HttpServletResponse) {
    response.addHeader("Expires", Expire)
    response.addHeader("Last-Modified", Expire)
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
    response.addHeader("Pragma", "no-cache")
  }

  errorHandler = {
    case t: Exception => {
      logger.error("Exception during client request processing", t)
      halt(500, "Internal server exception")
    }
  }

}