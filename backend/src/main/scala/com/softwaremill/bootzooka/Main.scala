package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.logging.Logging
import ox.logback.InheritableMDC
import ox.{Ox, OxApp, never}
import org.slf4j.bridge.SLF4JBridgeHandler
import ox.OxApp.Settings
import ox.otel.context.PropagatingVirtualThreadFactory

object Main extends OxApp.Simple with Logging:
  // route JUL to SLF4J (JUL is used by Magnum for logging)
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  // https://ox.softwaremill.com/latest/integrations/mdc-logback.html
  InheritableMDC.init

  Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

  // https://ox.softwaremill.com/latest/integrations/otel-context.html
  override protected def settings: Settings = Settings.Default.copy(threadFactory = PropagatingVirtualThreadFactory())

  override def run(using Ox): Unit =
    val deps = Dependencies.create

    deps.emailService.startProcesses()
    deps.httpApi.start()
    logger.info(s"Bootzooka started")

    // blocking until the application is shut down
    never
