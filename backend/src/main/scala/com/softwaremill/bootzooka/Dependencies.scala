package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.email.{EmailModel, EmailService, EmailTemplates}
import com.softwaremill.bootzooka.http.{Http, HttpApi}
import com.softwaremill.bootzooka.infrastructure.{DB, SetCorrelationIdBackend}
import com.softwaremill.bootzooka.metrics.{Metrics, VersionApi}
import com.softwaremill.bootzooka.passwordreset.{PasswordResetApi, PasswordResetAuthToken, PasswordResetCodeModel, PasswordResetService}
import com.softwaremill.bootzooka.security.{ApiKeyAuthToken, ApiKeyModel, ApiKeyService, Auth}
import com.softwaremill.bootzooka.user.{UserApi, UserModel, UserService}
import com.softwaremill.bootzooka.util.{Clock, DefaultClock, DefaultIdGenerator, IdGenerator}
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

trait Dependencies(using Ox, IO):
  // TODO use macwire/autowire once available for Scala3
  lazy val config: Config = Config.read.tap(Config.log)
  lazy val otel: OpenTelemetry = createOtel()
  lazy val metrics = new Metrics(otel)
  lazy val sttpBackend: SttpBackend[Identity, Any] =
    useInScope(
      Slf4jLoggingBackend(OpenTelemetryMetricsBackend(new SetCorrelationIdBackend(HttpClientSyncBackend()), otel), includeTiming = true)
    )(_.close())
  lazy val db: DB = useCloseableInScope(DB.createTestMigrate(config.db))
  lazy val idGenerator: IdGenerator = DefaultIdGenerator
  lazy val clock: Clock = DefaultClock
  lazy val http = new Http
  lazy val emailTemplates = new EmailTemplates
  lazy val emailModel = new EmailModel
  lazy val emailSender: EmailSender = EmailSender.create(sttpBackend, config.email)
  lazy val emailService = new EmailService(emailModel, idGenerator, emailSender, config.email, db, metrics)
  lazy val apiKeyModel = new ApiKeyModel
  lazy val apiKeyAuthToken = new ApiKeyAuthToken(apiKeyModel)
  lazy val apiKeyService = new ApiKeyService(apiKeyModel, idGenerator, clock)
  lazy val apiKeyAuth = new Auth(apiKeyAuthToken, db, clock)
  lazy val passwordResetCodeModel = new PasswordResetCodeModel
  lazy val passwordResetAuthToken = new PasswordResetAuthToken(passwordResetCodeModel)
  lazy val passwordResetAuth = new Auth(passwordResetAuthToken, db, clock)
  lazy val userModel = new UserModel
  lazy val userService = new UserService(userModel, emailService, emailTemplates, apiKeyService, idGenerator, clock, config.user)
  lazy val userApi = new UserApi(http, apiKeyAuth, userService, db, metrics)
  lazy val passwordResetService = new PasswordResetService(
    userModel,
    passwordResetCodeModel,
    emailService,
    emailTemplates,
    passwordResetAuth,
    idGenerator,
    config.passwordReset,
    clock,
    db
  )
  lazy val passwordResetApi = new PasswordResetApi(http, passwordResetService, db)
  lazy val versionApi = new VersionApi(http)
  lazy val httpApi =
    new HttpApi(http, userApi.endpoints ++ passwordResetApi.endpoints, List(versionApi.versionEndpoint), otel, config.api)

  private def createOtel(): OpenTelemetry =
    // An exporter that sends metrics to a collector over gRPC
    val grpcExporter = OtlpGrpcMetricExporter.builder().build()
    // A metric reader that exports using the gRPC exporter
    val metricReader: PeriodicMetricReader = PeriodicMetricReader.builder(grpcExporter).build()
    // A meter registry whose meters are read by the above reader
    val meterProvider: SdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader).build()
    // An instance of OpenTelemetry using the above meter registry
    OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build()
