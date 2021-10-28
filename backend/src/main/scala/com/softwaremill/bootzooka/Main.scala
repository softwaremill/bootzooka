package com.softwaremill.bootzooka

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.infrastructure.CorrelationId
import com.softwaremill.bootzooka.metrics.Metrics
import com.typesafe.scalalogging.StrictLogging
import doobie.util.transactor
import sttp.client3.SttpBackend

object Main extends StrictLogging {
  def main(args: Array[String]): Unit = {
    Metrics.init()
    Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

    val initModule = new InitModule {}
    initModule.logConfig()

    val mainTask = initModule.db.transactorResource.use { _xa =>
      initModule.baseSttpBackend.use { _baseSttpBackend =>
        val modules = new MainModule {
          override def xa: transactor.Transactor[IO] = _xa
          override def baseSttpBackend: SttpBackend[IO, Any] = _baseSttpBackend
          override def config: Config = initModule.config
        }

        /*
        Sequencing two tasks using the >> operator:
        - the first starts the background processes (such as an email sender)
        - the second allocates the http api resource, and never releases it (so that the http server is available
          as long as our application runs)
         */
        modules.startBackgroundProcesses >> modules.httpApi.resource.use(_ => IO.never)
      }
    }
    mainTask.unsafeRunSync()
  }
}
