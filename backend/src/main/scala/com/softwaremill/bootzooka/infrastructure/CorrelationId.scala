package com.softwaremill.bootzooka.infrastructure

import com.softwaremill.correlator.Http4sCorrelationMiddleware
import sttp.client3._
import sttp.client3.{Response, SttpBackend}
import monix.eval.Task
import sttp.capabilities.Effect
import sttp.monad.MonadError

/**
  * Correlation id support. The `init()` method should be called when the application starts.
  * See [[https://github.com/softwaremill/correlator]] for details.
  */
object CorrelationId extends com.softwaremill.correlator.CorrelationIdDecorator()

/**
  * An sttp backend wrapper, which sets the current correlation id on all outgoing requests.
  */
class SetCorrelationIdBackend[P](delegate: SttpBackend[Task, P]) extends SttpBackend[Task, P] {
  override def send[T, R >: P with Effect[Task]](request: Request[T, R]): Task[Response[T]] = {
    // suspending the calculation of the correlation id until the request send is evaluated
    CorrelationId()
      .map {
        case Some(cid) => request.header(Http4sCorrelationMiddleware.HeaderName, cid)
        case None      => request
      }
      .flatMap(delegate.send)
  }

  override def close(): Task[Unit] = delegate.close()

  override def responseMonad: MonadError[Task] = delegate.responseMonad
}
