package com.softwaremill.bootzooka.infrastructure

import sttp.client.{NothingT, SttpBackend}
import sttp.client.prometheus.PrometheusBackend
import monix.eval.Task

trait InfrastructureModule {
  implicit lazy val sttpBackend: SttpBackend[Task, Nothing, NothingT] = new SetCorrelationIdBackend(
    new LoggingSttpBackend[Task, Nothing, NothingT](PrometheusBackend[Task, Nothing](baseSttpBackend))
  )

  def baseSttpBackend: SttpBackend[Task, Nothing, NothingT]
}
