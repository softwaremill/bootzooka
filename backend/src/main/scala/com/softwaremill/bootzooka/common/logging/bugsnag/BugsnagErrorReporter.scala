package com.softwaremill.bootzooka.common.logging.bugsnag

import ch.qos.logback.classic.spi.ILoggingEvent
import com.bugsnag.{Client, MetaData}
import com.softwaremill.bootzooka.common.config.ConfigWithDefault
import com.softwaremill.bootzooka.common.logging.{DummyErrorReporter, ErrorReporter}
import com.typesafe.scalalogging.LazyLogging

/**
 * Simple wrapper for Bugsnag's Client API
 * @param apiKey valid api key. Must be non-null.
 */
class BugsnagErrorReporter(private val apiKey: String) extends ErrorReporter {

  private val bugsnagClient = new Client(apiKey)

  override def report(ex: Throwable, loggingEvent: ILoggingEvent): Unit = {
    val metaData = new MetaData()
    metaData.addToTab("Exception message", loggingEvent.getMessage)
    bugsnagClient.notify(ex, metaData)
  }

}

object BugsnagErrorReporter extends LazyLogging {

  val configApiKeyPath = "errorReporting.bugsnag.apiKey"

  /**
   * Create BugsnagErrorReporter instance
   *
   * @param config an instance of configuration holder
   * @return BugsnagErrorReporter if api key is present and is non empty or DummyErrorReporter otherwise
   */
  def apply(config: ConfigWithDefault): ErrorReporter =
    config.getOptionalString(configApiKeyPath)
      .filterNot(_.isEmpty)
      .map(new BugsnagErrorReporter(_))
      .getOrElse {
        logger.info("Missing or invalid Bugsnag API Key - falling back to DummyErrorReporter.")
        DummyErrorReporter
      }

}
