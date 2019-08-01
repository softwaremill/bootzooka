package com.softwaremill.bootzooka.util

import java.time.Clock

import com.softwaremill.bootzooka.config.Config

trait BaseModule {
  def idGenerator: IdGenerator
  def clock: Clock
  def config: Config
}
