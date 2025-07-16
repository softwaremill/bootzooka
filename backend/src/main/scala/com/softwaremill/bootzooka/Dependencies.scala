package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.admin.VersionApi
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.email.EmailService
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.http.{HttpApi, HttpConfig}
import com.softwaremill.bootzooka.infrastructure.DB
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.passwordreset.{PasswordResetApi, PasswordResetAuthToken}
import com.softwaremill.bootzooka.security.{ApiKeyAuthToken, ApiKeyService, Auth}
import com.softwaremill.bootzooka.user.UserApi
import com.softwaremill.bootzooka.util.{Clock, DefaultClock, DefaultIdGenerator, IdGenerator}
import com.softwaremill.macwire.{autowire, autowireMembersOf}
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import io.opentelemetry.instrumentation.runtimemetrics.java8.{Classes, Cpu, GarbageCollector, MemoryPools, Threads}
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import ox.{Ox, discard, tap, useCloseableInScope, useInScope}
import sttp.client4.SyncBackend
import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.client4.logging.slf4j.Slf4jLoggingBackend
import sttp.client4.opentelemetry.{OpenTelemetryMetricsBackend, OpenTelemetryTracingBackend}
import sttp.tapir.AnyEndpoint

case class Dependencies(httpApi: HttpApi, emailService: EmailService)

object Dependencies:
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
    AutoConfiguredOpenTelemetrySdk
      .initialize()
      .getOpenTelemetrySdk()
      .tap { otel =>
        // see https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/runtime-telemetry/runtime-telemetry-java8/library
        Classes.registerObservers(otel)
        Cpu.registerObservers(otel)
        MemoryPools.registerObservers(otel)
        Threads.registerObservers(otel)
        GarbageCollector.registerObservers(otel, false).discard
      }
      .tap(OpenTelemetryAppender.install)
