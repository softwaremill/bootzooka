package com.softwaremill.bootzooka

import cats.data.NonEmptyList
import com.softwaremill.bootzooka.email.EmailModule
import com.softwaremill.bootzooka.http.HttpAPIModule
import com.softwaremill.bootzooka.infrastructure.InfrastructureModule
import com.softwaremill.bootzooka.metrics.MetricsModule
import com.softwaremill.bootzooka.passwordreset.PasswordResetModule
import com.softwaremill.bootzooka.security.SecurityModule
import com.softwaremill.bootzooka.user.UserModule
import com.softwaremill.bootzooka.util.{Clock, DefaultClock, DefaultIdGenerator, IdGenerator, ServerEndpoints}
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
    with HttpAPIModule
    with InfrastructureModule {

  override lazy val idGenerator: IdGenerator = DefaultIdGenerator
  override lazy val clock: Clock = DefaultClock

  lazy val endpoints: ServerEndpoints = userApi.endpoints concatNel passwordResetApi.endpoints
  lazy val adminEndpoints: ServerEndpoints = NonEmptyList.of(metricsApi.metricsEndpoint, versionApi.versionEndpoint)
  lazy val backgroundProcesses: fs2.Stream[Task, Nothing] = fs2.Stream.eval_(emailService.startSender())
}
