package com.softwaremill.bootzooka.metrics

import cats.effect.IO

import java.io.StringWriter
import com.softwaremill.bootzooka.http.{Error_OUT, Http}
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint

/** Defines an endpoint which exposes the current state of the metrics, which can be later read by a Prometheus server.
  */
class MetricsApi(http: Http, registry: CollectorRegistry) {
  import http._

  val metricsEndpoint: ServerEndpoint[Unit, (StatusCode, Error_OUT), String, Any, IO] = baseEndpoint.get
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
