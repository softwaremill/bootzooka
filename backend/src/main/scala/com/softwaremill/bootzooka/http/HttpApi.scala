package com.softwaremill.bootzooka.http

import com.softwaremill.bootzooka.OpenAPIDescription
import com.softwaremill.bootzooka.infrastructure.SetTraceIdInMDCInterceptor
import com.softwaremill.bootzooka.logging.Logging
import io.opentelemetry.api.OpenTelemetry
import ox.Ox
import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.files.{FilesOptions, staticResourcesGetServerEndpoint}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.metrics.opentelemetry.OpenTelemetryMetrics
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.server.netty.NettyConfig
import sttp.tapir.server.netty.sync.{NettySyncServer, NettySyncServerBinding, NettySyncServerOptions}
import sttp.tapir.server.tracing.opentelemetry.OpenTelemetryTracing
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

/** Exposes the endpoints (defined using Tapir) using a Netty-based server, adding CORS, metrics, docs support.
  *
  * The following paths are exposed:
  *   - `/api/v1` - the API endpoints
  *   - `/api/v1/docs` - Swagger UI for the API
  *   - `/` - frontend resources
  *
  * @param serverEndpoints
  *   Endpoints to be exposed by the server
  * @param endpointsForDocs
  *   Endpoints for which documentation is exposed. Should contain the same endpoints as [[serverEndpoints]], but possibly with additional
  *   metadata
  */
class HttpApi(
    serverEndpoints: List[ServerEndpoint[Any, Identity]],
    endpointsForDocs: List[AnyEndpoint],
    otel: OpenTelemetry,
    config: HttpConfig
) extends Logging:
  private val apiContextPath = List("api", "v1")

  private val serverOptions: NettySyncServerOptions = NettySyncServerOptions.customiseInterceptors
    .prependInterceptor(OpenTelemetryTracing(otel))
    .prependInterceptor(SetTraceIdInMDCInterceptor)
    // all errors are formatted as JSON, and no additional routes are added to the server
    .defaultHandlers(msg => ValuedEndpointOutput(Http.jsonErrorOutOutput, Error_OUT(msg)), notFoundWhenRejected = true)
    .corsInterceptor(CORSInterceptor.default[Identity])
    .metricsInterceptor(OpenTelemetryMetrics.default[Identity](otel).metricsInterceptor())
    .options

  val allEndpoints: List[ServerEndpoint[Any, Identity]] =
    // The /api/v1 context path is added using Swagger's options, not to the endpoints.
    val docsEndpoints = SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.copy(contextPath = apiContextPath))
      .fromEndpoints[Identity](endpointsForDocs, OpenAPIDescription.Title, OpenAPIDescription.Version)

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
  end allEndpoints

  def start()(using Ox): NettySyncServerBinding =
    NettySyncServer(serverOptions, NettyConfig.default.host(config.host).port(config.port)).addEndpoints(allEndpoints).start()
end HttpApi
