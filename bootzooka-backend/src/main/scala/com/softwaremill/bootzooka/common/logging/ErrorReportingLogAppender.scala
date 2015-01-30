package com.softwaremill.bootzooka.common.logging

import java.util.concurrent.Executors

import ch.qos.logback.classic.spi.{ILoggingEvent, ThrowableProxy}
import ch.qos.logback.classic.{Level, LoggerContext}
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.{AppenderAttachable, FilterReply}
import com.softwaremill.bootzooka.common.config.ConfigWithDefault
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

/**
 * SLF4J Logger Appender that asynchronously forwards all error messages to given ErrorReporter.
 *
 * @param reporter wrapper for particular concrete error reporting service
 */
class ErrorReportingLogAppender(reporter: ErrorReporter)(implicit ec: ExecutionContext) extends AppenderBase[ILoggingEvent] {

  private val onlyErrorsFilter: Filter[ILoggingEvent] = new Filter[ILoggingEvent] {
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
    addFilter(onlyErrorsFilter)
    start()
    registerAsAppenderFor(Logger.ROOT_LOGGER_NAME)
  }

  private def registerAsAppenderFor(loggerName: String): Unit = {
    getAvailableLoggers.foreach { logger =>
      logger.asInstanceOf[AppenderAttachable[ILoggingEvent]].addAppender(this)
    }
  }

  private def getAvailableLoggers: Set[Logger] = {
    val defaultRootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
    LoggerFactory.getILoggerFactory match {
      case c: LoggerContext =>
        import scala.collection.JavaConversions._
        c.getLoggerList.toSet + defaultRootLogger
      case _ => Set(defaultRootLogger)
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

object ErrorReportingLogAppender {

  def apply(config: ConfigWithDefault, reporter: ErrorReporter): ErrorReportingLogAppender = {
    val ec = {
      val poolSize = config.getInt("errorReporting.thread-pool-size", 10)
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(poolSize))
    }
    new ErrorReportingLogAppender(reporter)(ec)
  }

}
