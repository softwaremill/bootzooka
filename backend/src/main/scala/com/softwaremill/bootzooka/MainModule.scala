package com.softwaremill.bootzooka

import java.time.Clock

import cats.data.NonEmptyList
import com.softwaremill.bootzooka.email.EmailModule
import com.softwaremill.bootzooka.http.{Http, HttpApi}
import com.softwaremill.bootzooka.infrastructure.InfrastructureModule
import com.softwaremill.bootzooka.metrics.MetricsModule
import com.softwaremill.bootzooka.passwordreset.PasswordResetModule
import com.softwaremill.bootzooka.security.SecurityModule
import com.softwaremill.bootzooka.user.UserModule
import com.softwaremill.bootzooka.util.{DefaultIdGenerator, IdGenerator, ServerEndpoints}
import monix.eval.Task

/**
  * Main application module. Depends on resources initialised in [[InitModule]].
  */
trait MainModule
    extends SecurityModule
    with EmailModule
    with UserModule
    with PasswordResetModule
    with MetricsModule
    with InfrastructureModule {

  override lazy val idGenerator: IdGenerator = DefaultIdGenerator
  override lazy val clock: Clock = Clock.systemUTC()

  lazy val http: Http = new Http()

  private lazy val endpoints: ServerEndpoints = userApi.endpoints concatNel passwordResetApi.endpoints
  private lazy val adminEndpoints: ServerEndpoints = NonEmptyList.of(metricsApi.metricsEndpoint, versionApi.versionEndpoint)

  lazy val httpApi: HttpApi = new HttpApi(http, endpoints, adminEndpoints, collectorRegistry, config.api)

  lazy val startBackgroundProcesses: Task[Unit] = emailService.startProcesses().void
}
