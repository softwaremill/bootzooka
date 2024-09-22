package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.admin.VersionApi
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.email.EmailService
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.http.{HttpApi, HttpConfig}
import com.softwaremill.bootzooka.infrastructure.{DB, SetCorrelationIdBackend}
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.passwordreset.{PasswordResetApi, PasswordResetAuthToken}
import com.softwaremill.bootzooka.security.{ApiKeyAuthToken, ApiKeyService, Auth}
import com.softwaremill.bootzooka.user.UserApi
import com.softwaremill.bootzooka.util.{Clock, DefaultClock, DefaultIdGenerator, IdGenerator}
import com.softwaremill.macwire.{autowire, autowireMembersOf}
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import ox.{IO, Ox, tap, useCloseableInScope, useInScope}
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.client3.opentelemetry.OpenTelemetryMetricsBackend
import sttp.client3.{HttpClientSyncBackend, SttpBackend}
import sttp.shared.Identity
import sttp.tapir.AnyEndpoint
import io.opentelemetry.instrumentation.jmx.engine.JmxMetricInsight
import com.softwaremill.bootzooka.metrics.JmxMetricInstaller

case class Dependencies(httpApi: HttpApi, emailService: EmailService)

object Dependencies:
  val endpointsForDocs: List[AnyEndpoint] = List(UserApi, PasswordResetApi, VersionApi).flatMap(_.endpointsForDocs)

  private case class Apis(userApi: UserApi, passwordResetApi: PasswordResetApi, versionApi: VersionApi):
    def endpoints = List(userApi, passwordResetApi, versionApi).flatMap(_.endpoints)

  def create(using Ox, IO): Dependencies =
    val config = Config.read.tap(Config.log)

    val autoOtelSdk = AutoConfiguredOpenTelemetrySdk.initialize()
    val otel = autoOtelSdk.getOpenTelemetrySdk()
    JmxMetricInstaller.initialize(autoOtelSdk)

    val sttpBackend = useInScope(
      Slf4jLoggingBackend(OpenTelemetryMetricsBackend(new SetCorrelationIdBackend(HttpClientSyncBackend()), otel), includeTiming = true)
    )(_.close())
    val db: DB = useCloseableInScope(DB.createTestMigrate(config.db))

    create(config, otel, sttpBackend, db, DefaultClock)

  /** Create the service graph using the given infrastructure services & configuration. */
  def create(config: Config, otel: OpenTelemetry, sttpBackend: SttpBackend[Identity, Any], db: DB, clock: Clock)(using IO): Dependencies =
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
