package com.softwaremill.bootzooka.infrastructure

import com.softwaremill.sttp.{MonadError, Request, Response, SttpBackend}
import com.typesafe.scalalogging.StrictLogging

class LoggingSttpBackend[R[_], S](delegate: SttpBackend[R, S]) extends SttpBackend[R, S] with StrictLogging {
  override def send[T](request: Request[T, S]): R[Response[T]] = {
    responseMonad.map(responseMonad.handleError(delegate.send(request)) {
      case e: Exception =>
        logger.error(s"Exception when sending request: $request", e)
        responseMonad.error(e)
    }) { response =>
      logger.debug(s"For request: $request got response: $response")
      response
    }
  }
  override def close(): Unit = delegate.close()
  override def responseMonad: MonadError[R] = delegate.responseMonad
}
