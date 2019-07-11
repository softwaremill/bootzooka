package com.softwaremill.bootzooka.metrics

import com.softwaremill.bootzooka.infrastructure.Http
import io.prometheus.client.CollectorRegistry

trait MetricsModule {
  lazy val metricsApi = new MetricsApi(http, collectorRegistry)
  lazy val versionApi = new VersionApi(http)

  def collectorRegistry: CollectorRegistry
  def http: Http
}
