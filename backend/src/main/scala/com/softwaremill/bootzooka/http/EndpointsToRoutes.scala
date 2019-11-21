package com.softwaremill.bootzooka.http

import com.softwaremill.bootzooka.Fail
import com.softwaremill.bootzooka.util.ServerEndpoints
import monix.eval.Task
import org.http4s.HttpRoutes
import sttp.model.StatusCode
import sttp.tapir.DecodeResult
import sttp.tapir.server.{DecodeFailureContext, DecodeFailureHandler, DecodeFailureHandling, ServerDefaults}
import sttp.tapir.server.http4s._
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.openapi.Server
import sttp.tapir.swagger.http4s.SwaggerHttp4s

class EndpointsToRoutes(http: Http, apiContextPath: String) {
  /**
    * Interprets the given endpoint descriptions as http4s routes
    */
  def apply(es: ServerEndpoints): HttpRoutes[Task] = {
    implicit val serverOptions: Http4sServerOptions[Task] = Http4sServerOptions
      .default[Task]
      .copy(
        decodeFailureHandler = decodeFailureHandler
      )
    es.toList.toRoutes
  }

  /**
    * tapir's Codecs parse inputs - query parameters, JSON bodies, headers - to their desired types. This might fail,
    * and then a decode failure is returned, instead of a value. How such a failure is handled can be customised.
    *
    * We want to return responses in the same JSON format (corresponding to [[Error_OUT]]) as other errors returned
    * during normal request processing.
    *
    * We use the default behavior of tapir (`ServerDefaults.decodeFailureHandler`), customising the format
    * used for returning errors (`http.failOutput`). This will cause `400 Bad Request` to be returned in most cases.
    *
    * Additionally, if the error thrown is a `Fail` we might get additional information, such as a custom status
    * code, by translating it using the `http.exceptionToErrorOut` method and using that to create the response.
    */
  private val decodeFailureHandler: DecodeFailureHandler = {
    def failResponse(code: StatusCode, msg: String): DecodeFailureHandling =
      DecodeFailureHandling.response(http.failOutput)((code, Error_OUT(msg)))

    val defaultHandler = ServerDefaults.decodeFailureHandler.copy(response = failResponse)

    {
      // if an exception is thrown when decoding an input, and the exception is a Fail, responding basing on the Fail
      case DecodeFailureContext(_, DecodeResult.Error(_, f: Fail)) =>
        DecodeFailureHandling.response(http.failOutput)(http.exceptionToErrorOut(f))
      // otherwise, converting the decode input failure into a response using tapir's defaults
      case ctx =>
        defaultHandler(ctx)
    }
  }

  /**
    * Interprets the given endpoint descriptions as docs, and returns http4s routes which expose the documentation
    * using Swagger.
    */
  def toDocsRoutes(es: ServerEndpoints): HttpRoutes[Task] = {
    val openapi = es.toList.toOpenAPI("Bootzooka", "1.0").copy(servers = List(Server(s"$apiContextPath", None)))
    val yaml = openapi.toYaml
    new SwaggerHttp4s(yaml).routes[Task]
  }
}
