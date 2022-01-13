package com.softwaremill.bootzooka.metrics

import cats.effect.IO
import com.softwaremill.bootzooka.http.Http
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import sttp.tapir.server.ServerEndpoint

import java.io.StringWriter

/** Defines an endpoint which exposes the current state of the metrics, which can be later read by a Prometheus server.
  */
class MetricsApi(http: Http, registry: CollectorRegistry) {
  import http._

  val metricsEndpoint: ServerEndpoint[Any, IO] = baseEndpoint.get
    .in("metrics")
    .out(stringBody)
    .serverLogic { _ =>
      IO {
        val writer = new StringWriter
        TextFormat.write004(writer, registry.metricFamilySamples)
        writer.toString
      }.toOut
    }
}
