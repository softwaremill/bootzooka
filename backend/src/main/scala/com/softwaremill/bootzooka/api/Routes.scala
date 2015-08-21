package com.softwaremill.bootzooka.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RejectionHandler, Route, ExceptionHandler}
import com.softwaremill.bootzooka.passwordreset.PasswordResetRoutes
import com.softwaremill.bootzooka.user.UsersRoutes

trait Routes extends UsersRoutes
    with PasswordResetRoutes
    with VersionRoutes
    with CacheSupport {

  private val exceptionHandler = ExceptionHandler {
    case e: Exception =>
      logger.error(s"Exception during client request processing: ${e.getMessage}", e)
      _.complete(StatusCodes.InternalServerError, "Internal server error")
  }

  val routes =
    logDuration {
      handleExceptions(exceptionHandler) {
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

  def logDuration(inner: Route): Route = { ctx =>
    val start = System.currentTimeMillis()
    // handling rejections here so that we get proper status codes
    val innerRejectionsHandled = handleRejections(RejectionHandler.default)(inner)
    mapResponse { resp =>
      val d = System.currentTimeMillis() - start
      logger.info(s"[${resp.status.intValue()}] ${ctx.request.method.name} ${ctx.request.uri} took: ${d}ms")
      resp
    }(innerRejectionsHandled)(ctx)
  }
}
