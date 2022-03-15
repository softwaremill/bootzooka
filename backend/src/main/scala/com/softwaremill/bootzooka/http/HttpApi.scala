package com.softwaremill.bootzooka.http

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import cats.implicits._
import com.softwaremill.bootzooka.util.ServerEndpoints
import com.typesafe.scalalogging.StrictLogging
import io.prometheus.client.CollectorRegistry
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.metrics.prometheus.Prometheus
import org.http4s.server.middleware.{CORS, Metrics}
import org.http4s.server.{Router, Server}
import org.http4s.{HttpApp, HttpRoutes}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.static.ResourcesOptions
import sttp.tapir.{emptyInput, resourcesGetServerEndpoint}

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
    endpoints: ServerEndpoints,
    adminEndpoints: ServerEndpoints,
    collectorRegistry: CollectorRegistry,
    config: HttpConfig
) extends StrictLogging {
  private val apiContextPath = List("api", "v1")
  private val endpointsToRoutes = new EndpointsToRoutes(http, apiContextPath)

  lazy val mainRoutes: HttpRoutes[IO] = endpointsToRoutes(endpoints)
  private lazy val adminRoutes: HttpRoutes[IO] = endpointsToRoutes(adminEndpoints)
  private lazy val docsRoutes: HttpRoutes[IO] = endpointsToRoutes.toDocsRoutes(endpoints)

  /** The resource describing the HTTP server; binds when the resource is allocated. */
  lazy val resource: Resource[IO, org.http4s.server.Server] = {
    val monitoredRoutes =
      Prometheus.metricsOps[IO](collectorRegistry).map(m => Metrics[IO](m)(mainRoutes))

    def buildApp(monitoredRoutes: HttpRoutes[IO]): HttpApp[IO] = Router(
      // for /api/v1 requests, first trying the API; then the docs; then, returning 404
      s"/${apiContextPath.mkString("/")}" -> {
        CORS.policy.withAllowOriginAll
          .withAllowCredentials(false)
          .apply(monitoredRoutes <+> docsRoutes)
      },
      "/admin" -> adminRoutes,
      // for all other requests, first trying getting existing webapp resource;
      // otherwise, returning index.html; this is needed to support paths in the frontend apps (e.g. /login)
      // the frontend app will handle displaying appropriate error messages
      "" -> webappRoutes
    ).orNotFound

    def buildServer(app: HttpApp[IO]): Resource[IO, Server] = BlazeServerBuilder[IO]
      .bindHttp(config.port, config.host)
      .withHttpApp(app)
      .resource

    monitoredRoutes.flatMap(routes => buildServer(buildApp(routes)))
  }

  /** Serves the webapp resources (html, js, css files), from the /webapp directory on the classpath. */
  private lazy val webappRoutes: HttpRoutes[IO] = {
    val loader = classOf[HttpApi].getClassLoader
    val indexEndpoint: ServerEndpoint[Any, IO] = resourcesGetServerEndpoint(emptyInput)(
      loader,
      "webapp",
      ResourcesOptions.default.defaultResource(List("index.html"))
    )
    endpointsToRoutes(NonEmptyList.one(indexEndpoint))
  }
}
