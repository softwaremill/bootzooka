package com.softwaremill.bootzooka.logging

import com.softwaremill.bootzooka.infrastructure.CorrelationIdSource
import com.typesafe.scalalogging.Logger
import org.slf4j.{LoggerFactory, MDC}

trait FLogging {
  private val delegate = Logger(LoggerFactory.getLogger(getClass.getName))
  protected def logger: FLogger = new FLogger(delegate)
}

class FLogger(delegate: Logger) {
  private val MDCKey = "cid"
  private def withMDC[F[_]: CorrelationIdSource, T](t: => T): F[T] =
    implicitly[CorrelationIdSource[F]].map { cid =>
      cid.foreach(x => MDC.put(MDCKey, x))
      try t
      finally MDC.remove(MDCKey)
    }

  def debug[F[_]: CorrelationIdSource](message: String): F[Unit] = withMDC(delegate.debug(message))
  def debug[F[_]: CorrelationIdSource](message: String, cause: Throwable): F[Unit] = withMDC(delegate.debug(message, cause))
  def info[F[_]: CorrelationIdSource](message: String): F[Unit] = withMDC(delegate.info(message))
  def info[F[_]: CorrelationIdSource](message: String, cause: Throwable): F[Unit] = withMDC(delegate.info(message, cause))
  def warn[F[_]: CorrelationIdSource](message: String): F[Unit] = withMDC(delegate.warn(message))
  def warn[F[_]: CorrelationIdSource](message: String, cause: Throwable): F[Unit] = withMDC(delegate.warn(message, cause))
  def error[F[_]: CorrelationIdSource](message: String): F[Unit] = withMDC(delegate.error(message))
  def error[F[_]: CorrelationIdSource](message: String, cause: Throwable): F[Unit] = withMDC(delegate.error(message, cause))
}
