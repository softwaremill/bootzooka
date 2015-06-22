package com.softwaremill.bootzooka.common.logging

import java.util.concurrent.Executors

import ch.qos.logback.classic.spi.{ILoggingEvent, ThrowableProxy}
import ch.qos.logback.classic.{Level, Logger, LoggerContext}
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import com.softwaremill.bootzooka.common.config.ConfigWithDefault
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
 * Logback Logger Appender that asynchronously forwards all error messages to given ErrorReporter.
 *
 * @param reporter wrapper for particular concrete error reporting service
 */
class AsyncErrorReportingLogAppender(reporter: ErrorReporter)(implicit ec: ExecutionContext) extends AppenderBase[ILoggingEvent] {

  private val errorOrGreaterLevelFilter: Filter[ILoggingEvent] = new Filter[ILoggingEvent] {
    override def decide(event: ILoggingEvent): FilterReply =
      if (event.getLevel.isGreaterOrEqual(Level.ERROR))
        FilterReply.ACCEPT
      else
        FilterReply.DENY
  }

  /**
   * Initialize and register this appender in all available Loggers.
   */
  def init(): Unit = {
    addFilter(errorOrGreaterLevelFilter)
    start()
    registerAsAppenderForAllLoggers()
  }

  private def registerAsAppenderForAllLoggers(): Unit =
    getAvailableLoggers.foreach(_.addAppender(this))

  private def getAvailableLoggers: Set[Logger] = {
    LoggerFactory.getILoggerFactory match {
      case c: LoggerContext =>
        import scala.collection.JavaConverters._
        c.getLoggerList.asScala.toSet
      case _ => Set.empty[Logger]
    }
  }

  /**
   * Extract data from logging event and pass it to given ErrorReporter
   * @param loggingEvent logging event
   */
  override def append(loggingEvent: ILoggingEvent): Unit =
    extractThrowable(loggingEvent).foreach { ex =>
      Future {
        reporter.report(ex, loggingEvent)
      }
    }

  private def extractThrowable(loggingEvent: ILoggingEvent): Option[Throwable] = {
    loggingEvent.getThrowableProxy match {
      case tp: ThrowableProxy => Some(tp.getThrowable)
      case _ => None
    }
  }

}

object AsyncErrorReportingLogAppender {

  def apply(config: ConfigWithDefault, reporter: ErrorReporter): AsyncErrorReportingLogAppender = {
    val ec = {
      val poolSize = config.getInt("errorReporting.thread-pool-size", 10)
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(poolSize))
    }
    new AsyncErrorReportingLogAppender(reporter)(ec)
  }

}
