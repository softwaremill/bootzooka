package com.softwaremill.bootzooka.http

import java.util.concurrent.Executors
import cats.data.{Kleisli, OptionT}
import cats.effect.{IO, Resource}
import cats.implicits._
import com.softwaremill.bootzooka.infrastructure.CorrelationId
import com.softwaremill.bootzooka.util.{Http4sCorrelationMiddleware, ServerEndpoints}
import com.typesafe.scalalogging.StrictLogging
import io.prometheus.client.CollectorRegistry
import org.http4s.{HttpApp, HttpRoutes, Request, Response, StaticFile}
import org.http4s.dsl.Http4sDsl
import org.http4s.metrics.prometheus.Prometheus
import org.http4s.server.Router
import org.http4s.server.middleware.{CORS, CORSConfig, Metrics}
import org.http4s.server.staticcontent.{ResourceService, _}
import org.http4s.syntax.kleisli._
import com.softwaremill.bootzooka.util.Http4sCorrelationMiddleware.source
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext

/** Interprets the endpoint descriptions (defined using tapir) as http4s routes, adding CORS, metrics, api docs and correlation id support.
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
  private val apiContextPath = "/api/v1"
  private val endpointsToRoutes = new EndpointsToRoutes(http, apiContextPath)

  lazy val mainRoutes: HttpRoutes[IO] =
    Http4sCorrelationMiddleware(CorrelationId).withCorrelationId(loggingMiddleware(endpointsToRoutes(endpoints)))
  private lazy val adminRoutes: HttpRoutes[IO] = endpointsToRoutes(adminEndpoints)
  private lazy val docsRoutes: HttpRoutes[IO] = endpointsToRoutes.toDocsRoutes(endpoints)

  private lazy val corsConfig: CORSConfig = CORSConfig.default

  /** The resource describing the HTTP server; binds when the resource is allocated.
    */
  lazy val resource: Resource[IO, org.http4s.server.Server] = {
    val prometheusHttp4sMetrics = Prometheus.metricsOps[IO](collectorRegistry)
    prometheusHttp4sMetrics
      .map(m => Metrics[IO](m)(mainRoutes))
      .flatMap { monitoredRoutes =>
        val app: HttpApp[IO] = Router(
          // for /api/v1 requests, first trying the API; then the docs; then, returning 404
          s"$apiContextPath" -> CORS(monitoredRoutes <+> docsRoutes <+> respondWithNotFound, corsConfig),
          "/admin" -> adminRoutes,
          // for all other requests, first trying getting existing webapp resource;
          // otherwise, returning index.html; this is needed to support paths in the frontend apps (e.g. /login)
          // the frontend app will handle displaying appropriate error messages
          "" -> (webappRoutes <+> respondWithIndex)
        ).orNotFound

        BlazeServerBuilder[IO]
          .bindHttp(config.port, config.host)
          .withHttpApp(app)
          .resource
      }
  }

  private def indexResponse(r: Request[IO]): IO[Response[IO]] =
    StaticFile.fromResource(s"/webapp/index.html", Some(r)).getOrElseF(IO.pure(Response.notFound[IO]))

  private val respondWithNotFound: HttpRoutes[IO] = Kleisli(_ => OptionT.pure(Response.notFound))
  private val respondWithIndex: HttpRoutes[IO] = Kleisli(req => OptionT.liftF(indexResponse(req)))

  private def loggingMiddleware(service: HttpRoutes[IO]): HttpRoutes[IO] = Kleisli { req: Request[IO] =>
    OptionT(for {
      _ <- IO(logger.debug(s"Starting request to: ${req.uri.path}"))
      r <- service(req).value
    } yield r)
  }

  /** Serves the webapp resources (html, js, css files), from the /webapp directory on the classpath.
    */
  private lazy val webappRoutes: HttpRoutes[IO] = {
    val dsl = Http4sDsl[IO]
    import dsl._
    val rootRoute = HttpRoutes.of[IO] { case request @ GET -> Root =>
      indexResponse(request)
    }
    val resourcesRoutes = resourceServiceBuilder[IO]("/webapp").toRoutes
    rootRoute <+> resourcesRoutes
  }
}
