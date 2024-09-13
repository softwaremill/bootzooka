package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.admin.VersionApi
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.email.{EmailModel, EmailService, EmailTemplates}
import com.softwaremill.bootzooka.http.{HttpApi, HttpConfig}
import com.softwaremill.bootzooka.infrastructure.{DB, SetCorrelationIdBackend}
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.passwordreset.{PasswordResetApi, PasswordResetAuthToken, PasswordResetCodeModel, PasswordResetService}
import com.softwaremill.bootzooka.security.{ApiKeyAuthToken, ApiKeyModel, ApiKeyService, Auth}
import com.softwaremill.bootzooka.user.{UserApi, UserModel, UserService}
import com.softwaremill.bootzooka.util.{Clock, DefaultClock, DefaultIdGenerator, Endpoints, IdGenerator}
import com.softwaremill.macwire.{autowire, membersOf}
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

case class Dependencies(httpApi: HttpApi, emailService: EmailService)

object Dependencies:
  val endpoints: Endpoints = UserApi.endpoints ++ PasswordResetApi.endpoints ++ VersionApi.endpoints

  def create(using Ox, IO): Dependencies =
    val config = Config.read.tap(Config.log)

    def sttpBackend(otel: OpenTelemetry): SttpBackend[Identity, Any] =
      useInScope(
        Slf4jLoggingBackend(OpenTelemetryMetricsBackend(new SetCorrelationIdBackend(HttpClientSyncBackend()), otel), includeTiming = true)
      )(_.close())

    val db: DB = useCloseableInScope(DB.createTestMigrate(config.db))

    create(config, sttpBackend, db, DefaultClock)

  def create(config: Config, sttpBackend: OpenTelemetry => SttpBackend[Identity, Any], db: DB, clock: Clock)(using Ox, IO): Dependencies =
    val otel = createOtel()
    autowire[Dependencies](
      membersOf(config),
      otel,
      sttpBackend(otel),
      db,
      DefaultIdGenerator,
      clock,
      EmailSender.create,
      (userApi: UserApi, passwordResetApi: PasswordResetApi, versionApi: VersionApi, otel: OpenTelemetry, httpConfig: HttpConfig) =>
        new HttpApi(
          userApi.serverEndpoints ++ passwordResetApi.serverEndpoints ++ versionApi.serverEndpoints,
          Dependencies.endpoints,
          otel,
          httpConfig
        ),
      classOf[ApiKeyAuthToken],
      classOf[PasswordResetAuthToken],
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
