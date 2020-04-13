package com.softwaremill.bootzooka.metrics

import io.prometheus.client.{Counter, Gauge, hotspot}

object Metrics {
  lazy val registeredUsersCounter: Counter =
    Counter
      .build()
      .name(s"bootzooka_registered_users_counter")
      .help(s"How many users registered on this instance since it was started")
      .register()

  lazy val emailQueueGauge: Gauge =
    Gauge
      .build()
      .name(s"bootzooka_email_queue_gauge")
      .help(s"How many emails are waiting to be sent")
      .register()

  def init(): Unit = {
    hotspot.DefaultExports.initialize()
  }
}
