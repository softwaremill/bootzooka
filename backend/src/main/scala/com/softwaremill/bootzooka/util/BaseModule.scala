package com.softwaremill.bootzooka.util

import cats.effect.Clock
import com.softwaremill.bootzooka.config.Config
import monix.eval.Task

trait BaseModule {
  def idGenerator: IdGenerator
  def clock: Clock[Task]
  def config: Config
}
