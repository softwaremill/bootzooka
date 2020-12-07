package com.softwaremill.bootzooka.util

import cats.effect.{Clock, IO}
import com.softwaremill.bootzooka.config.Config

trait BaseModule {
  def idGenerator: IdGenerator
  def clock: Clock[IO]
  def config: Config
}
