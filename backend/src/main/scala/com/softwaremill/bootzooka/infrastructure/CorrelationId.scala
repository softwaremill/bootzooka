package com.softwaremill.bootzooka.infrastructure

import com.softwaremill.sttp
import com.softwaremill.sttp.{MonadError, Response, SttpBackend}
import monix.eval.Task

/**
  * Correlation id support. The `init()` method should be called when the application starts.
  * See [[https://github.com/softwaremill/correlator]] for details.
  */
object CorrelationId extends com.softwaremill.correlator.CorrelationId()

/**
  * An sttp backend wrapper, which sets the current correlation id on all outgoing requests.
  */
class SetCorrelationIdBackend(delegate: SttpBackend[Task, Nothing]) extends SttpBackend[Task, Nothing] {
  override def send[T](request: sttp.Request[T, Nothing]): Task[Response[T]] = {
    // suspending the calculation of the correlation id until the request send is evaluated
    CorrelationId()
      .map {
        case Some(cid) => request.header(CorrelationId.headerName, cid)
        case None      => request
      }
      .flatMap(delegate.send)
  }

  override def close(): Unit = delegate.close()

  override def responseMonad: MonadError[Task] = delegate.responseMonad
}
