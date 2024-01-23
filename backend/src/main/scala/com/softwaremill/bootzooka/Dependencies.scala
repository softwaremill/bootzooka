package com.softwaremill.bootzooka

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.email.EmailService
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.http.{Http, HttpApi, HttpConfig}
import com.softwaremill.bootzooka.metrics.VersionApi
import com.softwaremill.bootzooka.passwordreset.{PasswordResetApi, PasswordResetAuthToken}
import com.softwaremill.bootzooka.security.ApiKeyAuthToken
import com.softwaremill.bootzooka.user.UserApi
import com.softwaremill.bootzooka.util.{Clock, DefaultIdGenerator}
import com.softwaremill.macwire.autocats.autowire
import com.webauthn4j.WebAuthnManager
import doobie.util.transactor.Transactor
import io.prometheus.client.CollectorRegistry
import sttp.client3.SttpBackend
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import com.softwaremill.bootzooka.passkeys.PasskeysService
import com.softwaremill.bootzooka.passkeys.PasskeysApi
import com.webauthn4j.converter.util.ObjectConverter

case class Dependencies(httpApi: HttpApi, emailService: EmailService)

object Dependencies {
  def wire(
      config: Config,
      sttpBackend: Resource[IO, SttpBackend[IO, Any]],
      xa: Resource[IO, Transactor[IO]],
      clock: Clock,
      collectorRegistry: CollectorRegistry
  ): Resource[IO, Dependencies] = {
    def buildHttpApi(
        http: Http,
        userApi: UserApi,
        passwordResetApi: PasswordResetApi,
        passkeysApi: PasskeysApi,
        versionApi: VersionApi,
        cfg: HttpConfig
    ) = {
      val prometheusMetrics = PrometheusMetrics.default[IO](registry = collectorRegistry)
      new HttpApi(
        http,
        userApi.endpoints concatNel passwordResetApi.endpoints concatNel passkeysApi.endpoints,
        NonEmptyList.of(versionApi.versionEndpoint),
        prometheusMetrics,
        cfg
      )
    }

    autowire[Dependencies](
      config.api,
      config.user,
      config.passwordReset,
      config.email,
      config.passkeys,
      WebAuthnManager.createNonStrictWebAuthnManager(),
      new ObjectConverter(),
      DefaultIdGenerator,
      clock,
      sttpBackend,
      xa,
      buildHttpApi _,
      new EmailService(_, _, _, _, _),
      EmailSender.create _,
      new ApiKeyAuthToken(_),
      new PasswordResetAuthToken(_),
      new PasskeysService(_, _, _, _)
    )
  }
}
