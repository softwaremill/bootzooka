package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.admin.VersionApi
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.email.EmailService
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.http.{HttpApi, HttpConfig}
import com.softwaremill.bootzooka.infrastructure.DB
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.passwordreset.{PasswordResetApi, PasswordResetAuthToken}
import com.softwaremill.bootzooka.security.{ApiKeyAuthToken, ApiKeyService, Auth}
import com.softwaremill.bootzooka.user.UserApi
import com.softwaremill.bootzooka.util.{Clock, DefaultClock, DefaultIdGenerator, IdGenerator}
import com.softwaremill.macwire.{autowire, autowireMembersOf}
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import io.opentelemetry.instrumentation.runtimemetrics.java17.RuntimeMetrics
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import ox.*
import sttp.client4.SyncBackend
import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.client4.logging.slf4j.Slf4jLoggingBackend
import sttp.client4.opentelemetry.{OpenTelemetryMetricsBackend, OpenTelemetryTracingBackend}
import sttp.tapir.AnyEndpoint

import scala.jdk.CollectionConverters.*

case class Dependencies(httpApi: HttpApi, emailService: EmailService)

object Dependencies extends Logging:
  val endpointsForDocs: List[AnyEndpoint] = List(UserApi, PasswordResetApi, VersionApi).flatMap(_.endpointsForDocs)

  private case class Apis(userApi: UserApi, passwordResetApi: PasswordResetApi, versionApi: VersionApi):
    def endpoints = List(userApi, passwordResetApi, versionApi).flatMap(_.endpoints)

  def create(using Ox): Dependencies =
    val config = Config.read.tap(Config.log)
    val otel = initializeOtel()
    val sttpBackend = useInScope(
      Slf4jLoggingBackend(OpenTelemetryMetricsBackend(OpenTelemetryTracingBackend(HttpClientSyncBackend(), otel), otel))
    )(_.close())
    val db: DB = useCloseableInScope(DB.createTestMigrate(config.db))

    create(config, otel, sttpBackend, db, DefaultClock)
  end create

  /** Create the service graph using the given infrastructure services & configuration. */
  def create(config: Config, otel: OpenTelemetry, sttpBackend: SyncBackend, db: DB, clock: Clock): Dependencies =
    autowire[Dependencies](
      autowireMembersOf(config),
      otel,
      sttpBackend,
      db,
      DefaultIdGenerator,
      clock,
      EmailSender.create,
      (apis: Apis, otel: OpenTelemetry, httpConfig: HttpConfig) =>
        new HttpApi(apis.endpoints, Dependencies.endpointsForDocs, otel, httpConfig),
      classOf[EmailService],
      new Auth(_: ApiKeyAuthToken, _: DB, _: Clock),
      new Auth(_: PasswordResetAuthToken, _: DB, _: Clock)
    )

  private def initializeOtel(): OpenTelemetry =
    val sdk = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk
    logOtel()
    sdk
      .tap(otel => RuntimeMetrics.create(otel).discard)
      .tap(OpenTelemetryAppender.install)

  private def logOtel(): Unit =
    def render(overrides: Seq[(String, String)]): String =
      if overrides.isEmpty then ""
      else "Overrides:\n" + overrides.sortBy(_._1).map((k, v) => s"$k=$v").mkString("\n")

    val overrides =
      System.getenv.asScala.toSeq.filter((k, _) => k.startsWith("OTEL_")) ++ sys.props.toSeq.filter((k, _) => k.startsWith("otel."))

    val message = if overrides.isEmpty then "No configuration overrides" else render(overrides)

    logger.info(
      s"""
         |OpenTelemetry configuration:
         |-----------------------
         |$message
         |For defaults refer to https://opentelemetry.io/docs/languages/java/configuration/
         |""".stripMargin
    )
  end logOtel

end Dependencies
