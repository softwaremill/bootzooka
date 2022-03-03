package com.softwaremill.bootzooka.http

import cats.effect.IO
import com.softwaremill.bootzooka.util.ServerEndpoints
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s._
import sttp.tapir.server.interceptor.ValuedEndpointOutput
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

class EndpointsToRoutes(http: Http, apiContextPath: List[String]) {

  /** Interprets the given endpoint descriptions as http4s routes */
  def apply(es: ServerEndpoints): HttpRoutes[IO] = {
    val serverOptions: Http4sServerOptions[IO, IO] = Http4sServerOptions
      .customInterceptors[IO, IO]
      .errorOutput(msg => ValuedEndpointOutput(http.jsonErrorOutOutput, Error_OUT(msg)))
      .options
    Http4sServerInterpreter(serverOptions).toRoutes(es.toList)
  }

  /** Interprets the given endpoint descriptions as docs, and returns http4s routes which expose the documentation using Swagger. */
  def toDocsRoutes(es: ServerEndpoints): HttpRoutes[IO] = {
    val swaggerEndpoints =
      SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.copy(contextPath = apiContextPath), customiseDocsModel = _.addServer(s"/${apiContextPath.mkString("/")}"))
        .fromServerEndpoints(es.toList, "My App", "1.0")
    Http4sServerInterpreter[IO]().toRoutes(swaggerEndpoints)
  }
}
