package com.softwaremill.bootzooka

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.email.EmailService
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.http.{Http, HttpApi, HttpConfig}
import com.softwaremill.bootzooka.metrics.{MetricsApi, VersionApi}
import com.softwaremill.bootzooka.passwordreset.{PasswordResetApi, PasswordResetAuthToken}
import com.softwaremill.bootzooka.security.ApiKeyAuthToken
import com.softwaremill.bootzooka.user.UserApi
import com.softwaremill.bootzooka.util.{Clock, DefaultIdGenerator}
import com.softwaremill.macwire.autocats.autowire
import doobie.util.transactor.Transactor
import io.prometheus.client.CollectorRegistry
import sttp.client3.SttpBackend

case class Dependencies(api: HttpApi, emailService: EmailService)

object Dependencies {
  def wire(config: Config, sttpBackend: Resource[IO, SttpBackend[IO, Any]], xa: Resource[IO, Transactor[IO]], clock: Clock): Resource[IO, Dependencies] = {
    def buildHttpApi(http: Http, userApi: UserApi, passwordResetApi: PasswordResetApi, metricsApi: MetricsApi, versionApi: VersionApi, collectorRegistry: CollectorRegistry, cfg: HttpConfig) =
      new HttpApi(
        http,
        userApi.endpoints concatNel passwordResetApi.endpoints,
        NonEmptyList.of(metricsApi.metricsEndpoint, versionApi.versionEndpoint),
        collectorRegistry,
        cfg)

    autowire[Dependencies](
      config.api,
      config.user,
      config.passwordReset,
      config.email,
      DefaultIdGenerator,
      clock,
      CollectorRegistry.defaultRegistry,
      sttpBackend,
      xa,
      buildHttpApi _,
      new ApiKeyAuthToken(_),
      new EmailService(_, _, _, _, _),
      EmailSender.create _,
      new PasswordResetAuthToken(_),
    )
  }
}
