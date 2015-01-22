package com.softwaremill.bootzooka.rest

import java.io.Writer
import java.util.Date
import javax.servlet.http.HttpServletResponse

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.commons.lang3.StringEscapeUtils._
import org.json4s.{DefaultFormats, Formats, _}
import org.scalatra._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}

class JsonServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult with LazyLogging with Halting {

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
    }
    halt(500, "Internal server exception")
  }

}