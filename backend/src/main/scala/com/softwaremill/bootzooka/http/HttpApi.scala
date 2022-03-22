package com.softwaremill.bootzooka.http

import cats.effect.{IO, Resource}
import com.softwaremill.bootzooka.infrastructure.CorrelationIdInterceptor
import com.softwaremill.bootzooka.logging.FLogger
import com.softwaremill.bootzooka.util.ServerEndpoints
import com.typesafe.scalalogging.StrictLogging
import io.prometheus.client.CollectorRegistry
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.static.ResourcesOptions
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir._

/** Interprets the endpoint descriptions (defined using tapir) as http4s routes, adding CORS, metrics, api docs support.
  *
  * The following endpoints are exposed:
  *   - `/api/v1` - the main API
  *   - `/api/v1/docs` - swagger UI for the main API
  *   - `/admin` - admin API
  *   - `/` - serving frontend resources
  */
class HttpApi(
    http: Http,
    mainEndpoints: ServerEndpoints,
    adminEndpoints: ServerEndpoints,
    collectorRegistry: CollectorRegistry,
    config: HttpConfig
) extends StrictLogging {
  private val apiContextPath = List("api", "v1")

  val serverOptions: Http4sServerOptions[IO, IO] = Http4sServerOptions
    .customInterceptors[IO, IO]
    // all errors are formatted as json
    .errorOutput(msg => ValuedEndpointOutput(http.jsonErrorOutOutput, Error_OUT(msg)))
    .serverLog {
      // using a context-aware logger for http logging
      val flogger = new FLogger(logger)
      Http4sServerOptions
        .defaultServerLog[IO]
        .doLogWhenHandled((msg, e) => e.fold(flogger.debug[IO](msg))(flogger.debug(msg, _)))
        .doLogAllDecodeFailures((msg, e) => e.fold(flogger.debug[IO](msg))(flogger.debug(msg, _)))
        .doLogExceptions((msg, e) => flogger.error[IO](msg, e))
        .doLogWhenReceived(msg => flogger.debug[IO](msg))
    }
    .corsInterceptor(CORSInterceptor.default[IO])
    .metricsInterceptor(PrometheusMetrics.default[IO](registry = collectorRegistry).metricsInterceptor())
    .options
    .prependInterceptor(CorrelationIdInterceptor) // TODO move to custom interceptors

  lazy val routes: HttpRoutes[IO] = {
    // creating the documentation using `mainEndpoints` without the /api/v1 context path; instead, a server will be added
    // with the appropriate suffix
    val docsEndpoints = SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.copy(contextPath = apiContextPath))
      .fromServerEndpoints(mainEndpoints.toList, "Bootzooka", "1.0")

    // for /api/v1 requests, first trying the API; then the docs
    val apiEndpoints = (mainEndpoints ++ docsEndpoints).map(se => se.prependSecurityIn(apiContextPath.foldLeft(emptyInput)(_ / _)))

    val allEndpoints = apiEndpoints ++
      adminEndpoints.map(_.prependSecurityIn("admin")).toList ++
      // for all other requests, first trying getting existing webapp resource (html, js, css files), from the /webapp
      // directory on the classpath; otherwise, returning index.html; this is needed to support paths in the frontend
      // apps (e.g. /login) the frontend app will handle displaying appropriate error messages
      List(
        resourcesGetServerEndpoint[IO](emptyInput)(
          classOf[HttpApi].getClassLoader,
          "webapp",
          ResourcesOptions.default.defaultResource(List("index.html"))
        )
      )

    Http4sServerInterpreter(serverOptions).toRoutes(allEndpoints.toList)
  }

  /** The resource describing the HTTP server; binds when the resource is allocated. */
  lazy val resource: Resource[IO, org.http4s.server.Server] = BlazeServerBuilder[IO]
    .bindHttp(config.port, config.host)
    .withHttpApp(routes.orNotFound)
    .resource
}
