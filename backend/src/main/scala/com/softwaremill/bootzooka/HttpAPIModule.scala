package com.softwaremill.bootzooka

import cats.effect.ExitCode
import com.softwaremill.bootzooka.infrastructure.{CorrelationId, Http}
import com.softwaremill.bootzooka.util.{BaseModule, ServerEndpoints}
import io.prometheus.client.CollectorRegistry
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.metrics.prometheus.Prometheus
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig, Metrics}
import org.http4s.syntax.kleisli._
import org.http4s.{HttpApp, HttpRoutes}
import tapir.docs.openapi._
import tapir.openapi.Server
import tapir.openapi.circe.yaml._
import tapir.server.http4s._
import tapir.swagger.http4s.SwaggerHttp4s

trait HttpAPIModule extends BaseModule {
  private val apiContextPath = "api/v1"
  private val docsContextPath = s"$apiContextPath/docs"

  def endpoints: ServerEndpoints
  def adminEndpoints: ServerEndpoints
  def http: Http

  lazy val httpRoutes: HttpRoutes[Task] = CorrelationId.setCorrelationIdMiddleware(toRoutes(endpoints))
  lazy val corsConfig: CORSConfig = CORS.DefaultCORSConfig
  lazy val docsRoutes: HttpRoutes[Task] = {
    val openapi = endpoints.toList.toOpenAPI("Bootzooka", "1.0").copy(servers = List(Server(s"/$apiContextPath", None)))
    val yaml = openapi.toYaml
    new SwaggerHttp4s(yaml, docsContextPath).routes[Task]
  }

  lazy val serveHttp: fs2.Stream[Task, ExitCode] = {
    implicit val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
    val prometheusHttp4sMetrics = Prometheus[Task](collectorRegistry)
    fs2.Stream
      .eval(prometheusHttp4sMetrics.map(m => Metrics[Task](m)(httpRoutes)))
      .flatMap { monitoredServices =>
        val app: HttpApp[Task] =
          Router(
            s"/$docsContextPath" -> docsRoutes,
            s"/$apiContextPath" -> CORS(monitoredServices, corsConfig),
            "/admin" -> toRoutes(adminEndpoints)
          ).orNotFound

        BlazeServerBuilder[Task]
          .bindHttp(config.api.port, config.api.host)
          .withHttpApp(app)
          .serve
      }
  }

  private def toRoutes(es: ServerEndpoints): HttpRoutes[Task] = {
    implicit val serverOptions: Http4sServerOptions[Task] = Http4sServerOptions
      .default[Task]
      .copy(
        decodeFailureHandler = http.decodeFailureHandler
      )
    es.toList.toRoutes
  }
}
