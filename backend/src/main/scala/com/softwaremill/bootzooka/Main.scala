package com.softwaremill.bootzooka

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import com.softwaremill.bootzooka.config.ConfigModule
import com.softwaremill.bootzooka.email.EmailService
import com.softwaremill.bootzooka.http.HttpApi
import com.softwaremill.bootzooka.infrastructure.DB
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.util.{Clock, DefaultClock}
import com.typesafe.scalalogging.StrictLogging
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.client3.prometheus.PrometheusBackend

object Main extends StrictLogging {
  def main(args: Array[String]): Unit = {
    Metrics.init()
    Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

    val configModule = new ConfigModule {}
    configModule.logConfig()
    val config = configModule.config

    lazy val clock: Clock = DefaultClock

    lazy val sttpBackend: Resource[IO, SttpBackend[IO, Fs2Streams[IO] with WebSockets]] =
      AsyncHttpClientFs2Backend
        .resource[IO]()
        .map(baseSttpBackend => Slf4jLoggingBackend(PrometheusBackend(baseSttpBackend), includeTiming = true))

    lazy val xa = new DB(config.db).transactorResource

    val mainTask = DependenciesFactory
      .resource(
        config = config,
        sttpBackend = sttpBackend,
        xa = xa,
        clock = clock
      )
      .use { case (httpApi, emailService) =>
        /*
      Sequencing two tasks using the >> operator:
      - the first starts the background processes (such as an email sender)
      - the second allocates the http api resource, and never releases it (so that the http server is available
        as long as our application runs)
         */
        emailService.startProcesses().void >> httpApi.resource.use(_ => IO.never)
      }

    mainTask.unsafeRunSync()
  }
}

case class Modules(emailService: EmailService, httpApi: HttpApi)
