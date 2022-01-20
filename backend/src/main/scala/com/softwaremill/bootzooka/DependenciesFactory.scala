package com.softwaremill.bootzooka

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.email.EmailService
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.http.{Http, HttpApi, HttpConfig}
import com.softwaremill.bootzooka.metrics.{MetricsApi, VersionApi}
import com.softwaremill.bootzooka.passwordreset.{PasswordResetApi, PasswordResetAuthToken}
import com.softwaremill.bootzooka.security.{ApiKeyAuthToken, ApiKeyModel}
import com.softwaremill.bootzooka.user.UserApi
import com.softwaremill.bootzooka.util.{Clock, DefaultIdGenerator, IdGenerator}
import com.softwaremill.macwire.autocats.autowire
import doobie.util.transactor.Transactor
import io.prometheus.client.CollectorRegistry
import sttp.client3.SttpBackend

object DependenciesFactory {
  private case class Modules(api: HttpApi, emailService: EmailService)

  def resource(config: Config, sttpBackend: Resource[IO, SttpBackend[IO, Any]], xa: Resource[IO, Transactor[IO]], clock: Clock): Resource[IO, (HttpApi, EmailService)] = {
    lazy val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
    lazy val idGenerator: IdGenerator = DefaultIdGenerator

    def buildHttpApi(http: Http, userApi: UserApi, passwordResetApi: PasswordResetApi, metricsApi: MetricsApi, versionApi: VersionApi, collectorRegistry: CollectorRegistry, cfg: HttpConfig) =
      new HttpApi(
        http,
        userApi.endpoints concatNel passwordResetApi.endpoints,
        NonEmptyList.of(metricsApi.metricsEndpoint, versionApi.versionEndpoint),
        collectorRegistry,
        cfg)

    def buildApiKeyAuthToken(apiKeyModel: ApiKeyModel): ApiKeyAuthToken = new ApiKeyAuthToken(apiKeyModel)

    autowire[Modules](
      config.api,
      config.user,
      config.passwordReset,
      config.email,
      idGenerator,
      clock,
      collectorRegistry,
      sttpBackend,
      xa,
      buildHttpApi _,
      buildApiKeyAuthToken _,
      new EmailService(_, _, _, _, _),
      EmailSender.create _,
      new PasswordResetAuthToken(_),
    ).map(modules => (modules.api, modules.emailService))
  }
}
