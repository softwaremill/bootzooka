package com.softwaremill.bootzooka.common.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import com.typesafe.scalalogging.LazyLogging

/**
 * Wrapper for concrete error reporting service's API.
 */
trait ErrorReporter {

  /**
   * Send exception and its meta information to the error reporting service.
   *
   * @param ex throwable
   * @param loggingEvent a event logging from which additional meta information may be extracted
   */
  def report(ex: Throwable, loggingEvent: ILoggingEvent): Unit

}

object DummyErrorReporter extends ErrorReporter with LazyLogging {

  override def report(ex: Throwable, loggingEvent: ILoggingEvent): Unit = ()

}
