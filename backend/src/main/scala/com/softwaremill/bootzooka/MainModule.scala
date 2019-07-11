package com.softwaremill.bootzooka

import cats.data.NonEmptyList
import com.softwaremill.bootzooka.email.EmailModule
import com.softwaremill.bootzooka.http.{Http, HttpAPIModule}
import com.softwaremill.bootzooka.infrastructure.InfrastructureModule
import com.softwaremill.bootzooka.metrics.MetricsModule
import com.softwaremill.bootzooka.passwordreset.PasswordResetModule
import com.softwaremill.bootzooka.security.SecurityModule
import com.softwaremill.bootzooka.user.UserModule
import com.softwaremill.bootzooka.util.{Clock, DefaultClock, DefaultIdGenerator, IdGenerator, ServerEndpoints}
import doobie.util.transactor.Transactor
import io.prometheus.client.CollectorRegistry
import monix.eval.Task

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
  override lazy val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
  override lazy val http: Http = new Http()

  lazy val endpoints: ServerEndpoints = userApi.endpoints concatNel passwordResetApi.endpoints
  lazy val adminEndpoints: ServerEndpoints = NonEmptyList.of(metricsApi.metricsEndpoint, versionApi.versionEndpoint)
  lazy val backgroundProcesses: fs2.Stream[Task, Nothing] = fs2.Stream.eval_(emailService.startSender())

  def xa: Transactor[Task]
}
