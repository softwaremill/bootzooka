package com.softwaremill.bootzooka

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.infrastructure.{CorrelationId, DB, SetCorrelationIdBackend}
import com.softwaremill.bootzooka.metrics.Metrics
import com.softwaremill.bootzooka.util.DefaultClock
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

    val config = Config.read
    Config.log(config)

    lazy val sttpBackend: Resource[IO, SttpBackend[IO, Fs2Streams[IO] with WebSockets]] =
      AsyncHttpClientFs2Backend
        .resource[IO]()
        .map(baseSttpBackend => Slf4jLoggingBackend(PrometheusBackend(new SetCorrelationIdBackend(baseSttpBackend)), includeTiming = true))

    val xa = new DB(config.db).transactorResource.map(CorrelationId.correlationIdTransactor)

    Dependencies
      .wire(config, sttpBackend, xa, DefaultClock)
      .use { case Dependencies(httpApi, emailService) =>
        /*
        Sequencing two tasks using the >> operator:
        - the first starts the background processes (such as an email sender)
        - the second allocates the http api resource, and never releases it (so that the http server is available
          as long as our application runs)
         */
        emailService.startProcesses().void >> httpApi.resource.use(_ => IO.never)
      }
      .unsafeRunSync()
  }
}
