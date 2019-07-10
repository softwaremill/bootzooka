package com.softwaremill.bootzooka.metrics

import java.io.StringWriter

import cats.implicits._
import com.softwaremill.bootzooka.infrastructure.Http
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import monix.eval.Task
import tapir.server.ServerEndpoint

class MetricsApi(http: Http, registry: CollectorRegistry) {
  import http._

  val metricsEndpoint: ServerEndpoint[Unit, Unit, String, Nothing, Task] = endpoint.get.out(stringBody).serverLogic { _ =>
    Task {
      val writer = new StringWriter
      TextFormat.write004(writer, registry.metricFamilySamples)
      writer.toString.asRight[Unit]
    }
  }
}
