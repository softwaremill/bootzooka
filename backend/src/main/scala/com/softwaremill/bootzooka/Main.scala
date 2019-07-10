package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.infrastructure.CorrelationId
import doobie.util.transactor
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

object Main {
  def main(args: Array[String]): Unit = {
    CorrelationId.init()

    val initModule = new InitModule {}
    initModule.logConfig()

    val mainTask = initModule.db.transactorResource.use { _xa =>
      val modules = new MainModule {
        override def xa: transactor.Transactor[Task] = _xa
        override def config: Config = initModule.config
      }

      (modules.backgroundProcesses ++ modules.serveHttp).compile.drain
    }

    mainTask.runSyncUnsafe()
  }
}
