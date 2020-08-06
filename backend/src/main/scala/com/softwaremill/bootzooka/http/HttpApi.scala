package com.softwaremill.bootzooka.http

import java.util.concurrent.Executors

import cats.data.{Kleisli, OptionT}
import cats.effect.{Blocker, Resource}
import cats.implicits._
import com.softwaremill.bootzooka.infrastructure.CorrelationId
import com.softwaremill.bootzooka.util.ServerEndpoints
import com.softwaremill.correlator.Http4sCorrelationMiddleware
import com.typesafe.scalalogging.StrictLogging
import io.prometheus.client.CollectorRegistry
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.{HttpApp, HttpRoutes, Request, Response, StaticFile}
import org.http4s.dsl.Http4sDsl
import org.http4s.metrics.prometheus.Prometheus
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig, Metrics}
import org.http4s.server.staticcontent.{ResourceService, _}
import org.http4s.syntax.kleisli._
import Http4sCorrelationMiddleware.source

import scala.concurrent.ExecutionContext

/**
  * Interprets the endpoint descriptions (defined using tapir) as http4s routes, adding CORS, metrics, api docs
  * and correlation id support.
  *
  * The following endpoints are exposed:
  * - `/api/v1` - the main API
  * - `/api/v1/docs` - swagger UI for the main API
  * - `/admin` - admin API
  * - `/` - serving frontend resources
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

  lazy val mainRoutes: HttpRoutes[Task] =
    Http4sCorrelationMiddleware(CorrelationId).withCorrelationId(loggingMiddleware(endpointsToRoutes(endpoints)))
  private lazy val adminRoutes: HttpRoutes[Task] = endpointsToRoutes(adminEndpoints)
  private lazy val docsRoutes: HttpRoutes[Task] = endpointsToRoutes.toDocsRoutes(endpoints)

  private lazy val corsConfig: CORSConfig = CORS.DefaultCORSConfig

  /**
    * The resource describing the HTTP server; binds when the resource is allocated.
    */
  lazy val resource: Resource[Task, org.http4s.server.Server[Task]] = {
    val prometheusHttp4sMetrics = Prometheus.metricsOps[Task](collectorRegistry)
    prometheusHttp4sMetrics
      .map(m => Metrics[Task](m)(mainRoutes))
      .flatMap { monitoredRoutes =>
        val app: HttpApp[Task] = Router(
          // for /api/v1 requests, first trying the API; then the docs; then, returning 404
          s"$apiContextPath" -> CORS(monitoredRoutes <+> docsRoutes <+> respondWithNotFound, corsConfig),
          "/admin" -> adminRoutes,
          // for all other requests, first trying getting existing webapp resource;
          // otherwise, returning index.html; this is needed to support paths in the frontend apps (e.g. /login)
          // the frontend app will handle displaying appropriate error messages
          "" -> (webappRoutes <+> respondWithIndex)
        ).orNotFound

        BlazeServerBuilder[Task]
          .bindHttp(config.port, config.host)
          .withHttpApp(app)
          .resource
      }
  }

  private val staticFileBlocker = Blocker.liftExecutionContext(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4)))

  private def indexResponse(r: Request[Task]): Task[Response[Task]] =
    StaticFile.fromResource(s"/webapp/index.html", staticFileBlocker, Some(r)).getOrElseF(Task.pure(Response.notFound[Task]))

  private val respondWithNotFound: HttpRoutes[Task] = Kleisli(_ => OptionT.pure(Response.notFound))
  private val respondWithIndex: HttpRoutes[Task] = Kleisli(req => OptionT.liftF(indexResponse(req)))

  private def loggingMiddleware(service: HttpRoutes[Task]): HttpRoutes[Task] = Kleisli { req: Request[Task] =>
    OptionT(for {
      _ <- Task(logger.debug(s"Starting request to: ${req.uri.path}"))
      r <- service(req).value
    } yield r)
  }

  /**
    * Serves the webapp resources (html, js, css files), from the /webapp directory on the classpath.
    */
  private lazy val webappRoutes: HttpRoutes[Task] = {
    val dsl = Http4sDsl[Task]
    import dsl._
    val rootRoute = HttpRoutes.of[Task] {
      case request @ GET -> Root => indexResponse(request)
    }
    val resourcesRoutes = resourceService[Task](ResourceService.Config("/webapp", staticFileBlocker))
    rootRoute <+> resourcesRoutes
  }
}
