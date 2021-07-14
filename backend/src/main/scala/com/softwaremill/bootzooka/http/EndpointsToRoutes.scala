package com.softwaremill.bootzooka.http

import com.softwaremill.bootzooka.util.ServerEndpoints
import monix.eval.Task
import org.http4s.HttpRoutes
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.Server
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server.http4s._
import sttp.tapir.swagger.http4s.SwaggerHttp4s

class EndpointsToRoutes(http: Http, apiContextPath: String) {

  /** Interprets the given endpoint descriptions as http4s routes
    */
  def apply(es: ServerEndpoints): HttpRoutes[Task] = {
    val serverOptions: Http4sServerOptions[Task, Task] = Http4sServerOptions
      .default[Task, Task]
    Http4sServerInterpreter(serverOptions).toRoutes(es.toList)
  }

  /** Interprets the given endpoint descriptions as docs, and returns http4s routes which expose the documentation
    * using Swagger.
    */
  def toDocsRoutes(es: ServerEndpoints): HttpRoutes[Task] = {
    val openapi = OpenAPIDocsInterpreter()
      .serverEndpointsToOpenAPI(es.toList,"Bootzooka", "1.0")
      .servers(List(Server(s"$apiContextPath", None)))
    val yaml = openapi.toYaml
    new SwaggerHttp4s(yaml).routes[Task]
  }
}
