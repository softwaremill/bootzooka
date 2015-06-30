package com.softwaremill.bootzooka.rest.serializers

import java.util.Date
import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import org.scalatra.ScalatraServlet

trait RequestLogger extends ScalatraServlet with LazyLogging {

  private var requestWithStartTime: Map[HttpServletRequest, Long] = Map()

  before() {
    synchronized {
      requestWithStartTime = requestWithStartTime + (request -> new Date().getTime)
    }
  }

  after() {
    val startOption: Option[Long] = requestWithStartTime.get(request)
    startOption match {
      case Some(start) => {
        logger.debug(s"Request to: ${request.getMethod} ${request.getRequestURI} served in: ${new Date().getTime - start} mills")
        request.getMethod
      }
      case _ =>
    }
    synchronized {
      requestWithStartTime = requestWithStartTime - request
    }
  }
}
