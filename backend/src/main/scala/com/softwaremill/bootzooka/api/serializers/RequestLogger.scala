package com.softwaremill.bootzooka.api.serializers

import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import org.scalatra.ScalatraServlet

import scala.collection._
import scala.collection.convert.decorateAsScala._

trait RequestLogger extends ScalatraServlet with LazyLogging {

  val requestWithStartTime: concurrent.Map[HttpServletRequest, Long] = new ConcurrentHashMap[HttpServletRequest, Long]().asScala

  before() {
    requestWithStartTime.put(request, new Date().getTime)
  }

  after() {
    val startOption: Option[Long] = requestWithStartTime.get(request)
    startOption match {
      case Some(start) => {
        logger.debug(s"Request to: ${request.getMethod} ${request.getRequestURI} served in: ${new Date().getTime - start} mills")
        requestWithStartTime.remove(request)
      }
      case _ => logger.error("Request not found.")
    }
  }
}
