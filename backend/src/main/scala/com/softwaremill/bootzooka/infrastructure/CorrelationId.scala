package com.softwaremill.bootzooka.infrastructure

import com.softwaremill.correlator.Http4sCorrelationMiddleware
import sttp.client._
import sttp.client.{Response, SttpBackend}
import monix.eval.Task
import sttp.client.monad.MonadError
import sttp.client.ws.WebSocketResponse

/**
  * Correlation id support. The `init()` method should be called when the application starts.
  * See [[https://github.com/softwaremill/correlator]] for details.
  */
object CorrelationId extends com.softwaremill.correlator.CorrelationIdDecorator()

/**
  * An sttp backend wrapper, which sets the current correlation id on all outgoing requests.
  */
class SetCorrelationIdBackend(delegate: SttpBackend[Task, Nothing, NothingT]) extends SttpBackend[Task, Nothing, NothingT] {
  override def send[T](request: Request[T, Nothing]): Task[Response[T]] = {
    // suspending the calculation of the correlation id until the request send is evaluated
    CorrelationId()
      .map {
        case Some(cid) => request.header(Http4sCorrelationMiddleware.HeaderName, cid)
        case None      => request
      }
      .flatMap(delegate.send)
  }

  override def openWebsocket[T, WS_RESULT](request: Request[T, Nothing], handler: NothingT[WS_RESULT]): Task[WebSocketResponse[WS_RESULT]] =
    delegate.openWebsocket(request, handler)

  override def close(): Task[Unit] = delegate.close()

  override def responseMonad: MonadError[Task] = delegate.responseMonad
}
