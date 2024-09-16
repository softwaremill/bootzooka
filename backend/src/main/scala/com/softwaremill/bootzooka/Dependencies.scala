package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.admin.VersionApi
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.email.EmailService
import com.softwaremill.bootzooka.http.{HttpApi, HttpConfig}
import com.softwaremill.bootzooka.infrastructure.{DB, SetCorrelationIdBackend}
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.passwordreset.{PasswordResetAuthToken, PasswordResetApi}
import com.softwaremill.bootzooka.security.{ApiKeyAuthToken, ApiKeyService, Auth}
import com.softwaremill.bootzooka.user.UserApi
import com.softwaremill.bootzooka.util.{Clock, DefaultClock, DefaultIdGenerator, IdGenerator}
import com.softwaremill.macwire.{autowire, autowireMembersOf}
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.`export`.PeriodicMetricReader
import ox.{IO, Ox, tap, useCloseableInScope, useInScope}
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.client3.opentelemetry.OpenTelemetryMetricsBackend
import sttp.client3.{HttpClientSyncBackend, SttpBackend}
import sttp.shared.Identity
import sttp.tapir.AnyEndpoint

case class Dependencies(httpApi: HttpApi, emailService: EmailService)

object Dependencies:
  val endpointsForDocs: List[AnyEndpoint] = List(UserApi, PasswordResetApi, VersionApi).flatMap(_.endpointsForDocs)

  private case class Apis(userApi: UserApi, passwordResetApi: PasswordResetApi, versionApi: VersionApi):
    def endpoints = List(userApi, passwordResetApi, versionApi).flatMap(_.endpoints)

  def create(using Ox, IO): Dependencies =
    val config = Config.read.tap(Config.log)
    val otel = createOtel()
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

  private def createOtel(): OpenTelemetry =
    // An exporter that sends metrics to a collector over gRPC
    val grpcExporter = OtlpGrpcMetricExporter.builder().build()
    // A metric reader that exports using the gRPC exporter
    val metricReader: PeriodicMetricReader = PeriodicMetricReader.builder(grpcExporter).build()
    // A meter registry whose meters are read by the above reader
    val meterProvider: SdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader).build()
    // An instance of OpenTelemetry using the above meter registry
    OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build()
