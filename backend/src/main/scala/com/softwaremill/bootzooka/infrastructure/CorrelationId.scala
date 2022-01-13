package com.softwaremill.bootzooka.infrastructure

import cats.effect.IO
import com.softwaremill.bootzooka.util.{CorrelationIdDecorator, Http4sCorrelationMiddleware}
import sttp.client3._
import sttp.client3.{Response, SttpBackend}
import sttp.capabilities.Effect
import sttp.monad.MonadError

/** Correlation id support. The `init()` method should be called when the application starts. See
  * [[https://github.com/softwaremill/correlator]] for details.
  */
object CorrelationId extends CorrelationIdDecorator()

/** An sttp backend wrapper, which sets the current correlation id on all outgoing requests.
  */
class SetCorrelationIdBackend[P](delegate: SttpBackend[IO, P]) extends SttpBackend[IO, P] {
  override def send[T, R >: P with Effect[IO]](request: Request[T, R]): IO[Response[T]] = {
    // suspending the calculation of the correlation id until the request send is evaluated
    CorrelationId()
      .map {
        case Some(cid) => request.header(Http4sCorrelationMiddleware.HeaderName, cid)
        case None      => request
      }
      .flatMap(delegate.send)
  }

  override def close(): IO[Unit] = delegate.close()

  override def responseMonad: MonadError[IO] = delegate.responseMonad
}
