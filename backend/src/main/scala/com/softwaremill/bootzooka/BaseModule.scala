package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.config.Config

trait BaseModule {
  def idGenerator: IdGenerator
  def clock: Clock
  def config: Config
}

