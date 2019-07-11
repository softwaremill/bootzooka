package com.softwaremill.bootzooka.infrastructure

import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.asynchttpclient.monix.AsyncHttpClientMonixBackend
import com.softwaremill.sttp.prometheus.PrometheusBackend
import monix.eval.Task

trait InfrastructureModule {
  private lazy val baseSttpBackend: SttpBackend[Task, Nothing] = AsyncHttpClientMonixBackend()
  implicit lazy val sttpBackend: SttpBackend[Task, Nothing] = new SetCorrelationIdBackend(
    new LoggingSttpBackend[Task, Nothing](PrometheusBackend[Task, Nothing](baseSttpBackend))
  )
}
