package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.logging.Logging
import ox.logback.InheritableMDC
import ox.{IO, Ox, OxApp, never}

object Main extends OxApp.Simple with Logging:
  InheritableMDC.init
  Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

  override def run(using Ox, IO): Unit =
    val deps = Dependencies.create

    deps.emailService.startProcesses()
    val binding = deps.httpApi.start()
    logger.info(s"Started Bootzooka on ${binding.hostName}:${binding.port}.")

    // blocking until the application is shut down
    never
