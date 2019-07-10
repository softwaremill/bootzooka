package com.softwaremill.bootzooka.metrics

import io.prometheus.client.{Gauge, hotspot}

object AppMetrics extends HotSpotMetrics {
  lazy val xGauge: Gauge =
    Gauge
      .build()
      .name(s"x")
      .help(s"X")
      .register()
}

trait HotSpotMetrics {
  hotspot.DefaultExports.initialize()
}
