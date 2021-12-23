package com.softwaremill.bootzooka.http

import cats.effect.IO
import com.softwaremill.bootzooka.Fail
import com.softwaremill.bootzooka.util.ServerEndpoints
import org.http4s.HttpRoutes
import sttp.model.{Header, StatusCode}
import sttp.tapir.server.http4s._
import sttp.tapir.server.interceptor.decodefailure.DefaultDecodeFailureHandler.{FailureMessages, respond}
import sttp.tapir.server.interceptor.decodefailure.{DecodeFailureHandler, DefaultDecodeFailureHandler}
import sttp.tapir.server.interceptor.{DecodeFailureContext, ValuedEndpointOutput}
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.{DecodeResult, headers}

class EndpointsToRoutes(http: Http, apiContextPath: String) {

  /** Interprets the given endpoint descriptions as http4s routes
    */
  def apply(es: ServerEndpoints): HttpRoutes[IO] = {
    val serverOptions: Http4sServerOptions[IO, IO] = Http4sServerOptions
      .customInterceptors[IO, IO]
      .decodeFailureHandler(decodeFailureHandler)
      .options
    Http4sServerInterpreter(serverOptions).toRoutes(es.toList)
  }

  /** tapir's Codecs parse inputs - query parameters, JSON bodies, headers - to their desired types. This might fail, and then a decode
    * failure is returned, instead of a value. How such a failure is handled can be customised.
    *
    * We want to return responses in the same JSON format (corresponding to [[Error_OUT]]) as other errors returned during normal request
    * processing.
    *
    * We use the default behavior of tapir (`ServerDefaults.decodeFailureHandler`), customising the format used for returning errors
    * (`http.failOutput`). This will cause `400 Bad Request` to be returned in most cases.
    *
    * Additionally, if the error thrown is a `Fail` we might get additional information, such as a custom status code, by translating it
    * using the `http.exceptionToErrorOut` method and using that to create the response.
    */
  private val decodeFailureHandler: DecodeFailureHandler = {
    def failResponse: (StatusCode, List[Header], String) => ValuedEndpointOutput[_] =
      (c, hs, m) => ValuedEndpointOutput(headers.and(http.failOutput), (hs, c, Error_OUT(m)))

    val defaultHandler = DefaultDecodeFailureHandler(
      respond(_, badRequestOnPathErrorIfPathShapeMatches = false, badRequestOnPathInvalidIfPathShapeMatches = true),
      FailureMessages.failureMessage,
      failResponse
    )

    {
      // if an exception is thrown when decoding an input, and the exception is a Fail, responding basing on the Fail
      case DecodeFailureContext(_, DecodeResult.Error(_, f: Fail), _, _) =>
        Some(ValuedEndpointOutput(http.failOutput, http.exceptionToErrorOut(f)))
      // otherwise, converting the decode input failure into a response using tapir's defaults
      case ctx =>
        defaultHandler(ctx)
    }
  }

  /** Interprets the given endpoint descriptions as docs, and returns http4s routes which expose the documentation using Swagger.
    */
  def toDocsRoutes(es: ServerEndpoints): HttpRoutes[IO] = {
    val swaggerEndpoints = SwaggerInterpreter(customiseDocsModel = _.addServer(s"$apiContextPath")).fromServerEndpoints(es.toList, "My App", "1.0")
   Http4sServerInterpreter[IO]().toRoutes(swaggerEndpoints)
  }
}
