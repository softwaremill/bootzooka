package com.softwaremill.bootzooka.logging

import org.slf4j.LoggerFactory

trait Logging:
  protected val logger = LoggerFactory.getLogger(getClass.getName)
