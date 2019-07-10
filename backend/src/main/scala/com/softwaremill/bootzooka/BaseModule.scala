package com.softwaremill.bootzooka

trait BaseModule {
  def idGenerator: IdGenerator
  def clock: Clock
  def config: Config
}

