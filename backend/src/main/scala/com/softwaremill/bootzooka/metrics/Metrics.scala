package com.softwaremill.bootzooka.metrics

import io.prometheus.client.{Counter, hotspot}

object Metrics {
  lazy val registeredUsersCounter: Counter =
    Counter
      .build()
      .name(s"bootzooka_registered_users_counter")
      .help(s"How many users registerd on this instance since it was started")
      .register()

  def init(): Unit = {
    hotspot.DefaultExports.initialize()
  }
}
