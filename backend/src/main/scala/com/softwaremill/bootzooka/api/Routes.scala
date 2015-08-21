package com.softwaremill.bootzooka.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler

trait Routes extends UsersRoutes
    with PasswordResetRoutes
    with VersionRoutes
    with CacheSupport {

  private val exceptionHandler = ExceptionHandler {
    case e: Exception =>
      logger.error(s"Exception during client request processing: ${e.getMessage}", e)
      _.complete(StatusCodes.InternalServerError, "Internal server error")
  }

  // TODO: request logger
  val routes = handleExceptions(exceptionHandler) {
    cacheImages {
      encodeResponse {
        pathPrefix("api") {
          passwordResetRoutes ~
            usersRoutes ~
            versionRoutes
        } ~
          getFromResourceDirectory("webapp") ~
          path("") {
            getFromResource("webapp/index.html")
          }
      }
    }
  }
}
