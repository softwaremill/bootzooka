package com.softwaremill.bootzooka.infrastructure

import cats.effect.IO
import sttp.client3.SttpBackend
import sttp.client3.prometheus.PrometheusBackend
import sttp.client3.logging.slf4j.Slf4jLoggingBackend

trait InfrastructureModule {
  implicit lazy val sttpBackend: SttpBackend[IO, Any] = new SetCorrelationIdBackend(
    Slf4jLoggingBackend(PrometheusBackend(baseSttpBackend), includeTiming = true)
  )

  def baseSttpBackend: SttpBackend[IO, Any]
}
