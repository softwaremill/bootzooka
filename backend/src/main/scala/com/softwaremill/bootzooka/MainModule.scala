package com.softwaremill.bootzooka

import cats.data.NonEmptyList
import com.softwaremill.bootzooka.email.EmailModule
import com.softwaremill.bootzooka.http.{Http, HttpApi}
import com.softwaremill.bootzooka.infrastructure.InfrastructureModule
import com.softwaremill.bootzooka.metrics.MetricsModule
import com.softwaremill.bootzooka.passwordreset.PasswordResetModule
import com.softwaremill.bootzooka.security.SecurityModule
import com.softwaremill.bootzooka.user.UserModule
import com.softwaremill.bootzooka.util.{Clock, DefaultClock, DefaultIdGenerator, IdGenerator}
import monix.eval.Task

/**
  * Main application module. Depends on resources initalised in [[InitModule]].
  */
trait MainModule
    extends SecurityModule
    with EmailModule
    with UserModule
    with PasswordResetModule
    with MetricsModule
    with InfrastructureModule {

  override lazy val idGenerator: IdGenerator = DefaultIdGenerator
  override lazy val clock: Clock = DefaultClock

  lazy val http: Http = new Http()
  lazy val httpApi: HttpApi = new HttpApi(
    http,
    userApi.endpoints concatNel passwordResetApi.endpoints,
    NonEmptyList.of(metricsApi.metricsEndpoint, versionApi.versionEndpoint),
    collectorRegistry,
    config.api
  )

  lazy val backgroundProcesses: fs2.Stream[Task, Nothing] = fs2.Stream.eval_(emailService.startProcesses())
}
