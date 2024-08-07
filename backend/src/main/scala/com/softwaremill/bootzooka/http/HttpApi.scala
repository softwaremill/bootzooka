package com.softwaremill.bootzooka.http

import com.softwaremill.bootzooka.OpenAPISpec
import com.softwaremill.bootzooka.infrastructure.CorrelationIdInterceptor
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.util.{Endpoints, ServerEndpoints}
import io.opentelemetry.api.OpenTelemetry
import ox.{IO, Ox}
import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.files.{FilesOptions, staticResourcesGetServerEndpoint}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.metrics.opentelemetry.OpenTelemetryMetrics
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.server.netty.NettyConfig
import sttp.tapir.server.netty.sync.{NettySyncServer, NettySyncServerBinding, NettySyncServerOptions}
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

/** Exposes the endpoint descriptions (defined using tapir) using a Netty-based server, adding CORS, metrics, api docs support.
  *
  * The following endpoints are exposed:
  *   - `/api/v1` - the API
  *   - `/api/v1/docs` - swagger UI for the API
  *   - `/` - serving frontend resources
  *
  * @param serverEndpoints
  *   Endpoints to be exposed by the server
  * @param endpointsForDocs
  *   Endpoints for which documentation is exposed. Should contain the same endpoints as `serverEndpoints`, but possibly with additional
  *   metadata
  */
class HttpApi(
    serverEndpoints: ServerEndpoints,
    endpointsForDocs: Endpoints,
    otel: OpenTelemetry,
    config: HttpConfig
) extends Logging:
  private val apiContextPath = List("api", "v1")

  private val serverOptions: NettySyncServerOptions = NettySyncServerOptions.customiseInterceptors
    .prependInterceptor(CorrelationIdInterceptor)
    // all errors are formatted as JSON, and there are no other additional routes
    .defaultHandlers(msg => ValuedEndpointOutput(Http.jsonErrorOutOutput, Error_OUT(msg)), notFoundWhenRejected = true)
    .corsInterceptor(CORSInterceptor.default[Identity])
    .metricsInterceptor(OpenTelemetryMetrics.default[Identity](otel).metricsInterceptor())
    .options

  val allEndpoints: List[ServerEndpoint[Any, Identity]] = {
    // Creating the documentation using `mainEndpoints`. The /api/v1 context path is added using Swagger's options, not to the endpoints.
    val docsEndpoints = SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.copy(contextPath = apiContextPath))
      .fromEndpoints[Identity](endpointsForDocs, OpenAPISpec.Title, OpenAPISpec.Version)

    // For /api/v1 requests, first trying the API; then the docs. Prepending the context path to each endpoint.
    val apiEndpoints =
      (serverEndpoints ++ docsEndpoints).map(se => se.prependSecurityIn(apiContextPath.foldLeft(emptyInput: EndpointInput[Unit])(_ / _)))

    // For all other requests, first trying getting existing webapp resource (html, js, css files), from the /webapp
    // directory on the classpath. Otherwise, returning index.html. This is needed to support paths in the frontend
    // apps (e.g. /login) the frontend app will handle displaying appropriate error messages
    val webappEndpoints = List(
      staticResourcesGetServerEndpoint[Identity](emptyInput: EndpointInput[Unit])(
        classOf[HttpApi].getClassLoader,
        "webapp",
        FilesOptions.default[Identity].defaultFile(List("index.html"))
      )
    )
    apiEndpoints ++ webappEndpoints
  }

  def start()(using Ox, IO): NettySyncServerBinding =
    NettySyncServer(serverOptions, NettyConfig.default.host(config.host).port(config.port)).addEndpoints(allEndpoints).start()
