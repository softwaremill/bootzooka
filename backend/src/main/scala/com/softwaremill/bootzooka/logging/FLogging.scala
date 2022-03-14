package com.softwaremill.bootzooka.logging

import cats.Functor
import cats.syntax.all._
import com.softwaremill.bootzooka.infrastructure.CorrelationIdSource
import com.typesafe.scalalogging.Logger
import org.slf4j.{LoggerFactory, MDC}

trait FLogging {
  private val delegate = Logger(LoggerFactory.getLogger(getClass.getName))
  protected def logger[F[_]: CorrelationIdSource: Functor]: FLogger[F] = new FLogger(delegate)
}

class FLogger[F[_]: CorrelationIdSource: Functor](delegate: Logger) {
  private val MDCKey = "cid"
  private def withMDC[T](t: => T): F[T] =
    implicitly[CorrelationIdSource[F]].get.map { cid =>
      cid.foreach(x => MDC.put(MDCKey, x))
      try t
      finally MDC.remove(MDCKey)
    }

  def debug(message: String): F[Unit] = withMDC(delegate.debug(message))
  def debug(message: String, cause: Throwable): F[Unit] = withMDC(delegate.debug(message, cause))
  def info(message: String): F[Unit] = withMDC(delegate.info(message))
  def info(message: String, cause: Throwable): F[Unit] = withMDC(delegate.info(message, cause))
  def warn(message: String): F[Unit] = withMDC(delegate.warn(message))
  def warn(message: String, cause: Throwable): F[Unit] = withMDC(delegate.warn(message, cause))
  def error(message: String): F[Unit] = withMDC(delegate.error(message))
  def error(message: String, cause: Throwable): F[Unit] = withMDC(delegate.error(message, cause))
}
