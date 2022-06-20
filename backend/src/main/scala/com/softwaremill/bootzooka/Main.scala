package com.softwaremill.bootzooka

import cats.effect.{IO, Resource, ResourceApp}
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.infrastructure.{CorrelationId, DB, Doobie, SetCorrelationIdBackend}
import com.softwaremill.bootzooka.util.DefaultClock
import com.typesafe.scalalogging.StrictLogging
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import sttp.client3.logging.slf4j.Slf4jLoggingBackend
import sttp.client3.prometheus.PrometheusBackend

object Main extends ResourceApp.Forever with StrictLogging {

  Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

  val sttpBackend: Resource[IO, SttpBackend[IO, Fs2Streams[IO] with WebSockets]] =
    AsyncHttpClientFs2Backend
      .resource[IO]()
      .map(baseSttpBackend => Slf4jLoggingBackend(PrometheusBackend(new SetCorrelationIdBackend(baseSttpBackend)), includeTiming = true))

  val config: Config = Config.read
  Config.log(config)

  val xa: Resource[IO, Doobie.Transactor[IO]] = new DB(config.db).transactorResource.map(CorrelationId.correlationIdTransactor)

  /*
    Sequencing two tasks:
      - the first starts the background processes (such as an email sender)
      - the second allocates the http api resource, and never releases it (so that the http server is available
        as long as our application runs)
   */
  override def run(list: List[String]): Resource[IO, Unit] = for {
    deps <- Dependencies.wire(config, sttpBackend, xa, DefaultClock)
    _ <- deps.emailService.startProcesses().background
    _ <- deps.httpApi.resource
  } yield ()
}
