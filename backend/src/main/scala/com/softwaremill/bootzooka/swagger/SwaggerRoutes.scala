package com.softwaremill.bootzooka.swagger

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.softwaremill.bootzooka.common.api.{JsonSupport, RoutesSupport}
import com.typesafe.scalalogging.StrictLogging

trait SwaggerRoutes extends RoutesSupport with StrictLogging with JsonSupport {
  def swaggerRoute = pathPrefix("swagger") {
    getFromResourceDirectory("swagger/dist") ~ pathEndOrSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect)))
  }
}
