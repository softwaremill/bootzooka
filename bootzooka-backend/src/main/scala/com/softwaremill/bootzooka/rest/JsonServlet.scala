package com.softwaremill.bootzooka.rest

import java.util.Date
import javax.servlet.http.HttpServletResponse

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.scalatra.swagger.SwaggerSupport

trait Mappable {

  val Prefix = "/rest/"

  def fullMappingPath = Prefix + mappingPath

  def mappingPath: String
}

trait SwaggerMappable {
  self: Mappable with SwaggerSupport =>

  def name = Prefix.tail + mappingPath
}

abstract class JsonServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult with LazyLogging with Halting with Mappable {

  protected implicit val jsonFormats: Formats = DefaultFormats

  val Expire = new Date().toString

  before() {
    contentType = formats("json")
    applyNoCache(response)
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