package com.softwaremill.bootzooka.infrastructure

import sttp.client3.SttpBackend
import sttp.client3.prometheus.PrometheusBackend
import monix.eval.Task
import sttp.client3.logging.slf4j.Slf4jLoggingBackend

trait InfrastructureModule {
  implicit lazy val sttpBackend: SttpBackend[Task, Any] = new SetCorrelationIdBackend(
    Slf4jLoggingBackend(PrometheusBackend(baseSttpBackend), includeTiming = true)
  )

  def baseSttpBackend: SttpBackend[Task, Any]
}
