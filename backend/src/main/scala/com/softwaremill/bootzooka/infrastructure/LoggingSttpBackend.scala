package com.softwaremill.bootzooka.infrastructure

import sttp.client.{Request, Response, SttpBackend}
import com.typesafe.scalalogging.StrictLogging
import sttp.client.monad.MonadError
import sttp.client.ws.WebSocketResponse

class LoggingSttpBackend[F[_], S, WS_HANDLER[_]](delegate: SttpBackend[F, S, WS_HANDLER])
    extends SttpBackend[F, S, WS_HANDLER]
    with StrictLogging {
  override def send[T](request: Request[T, S]): F[Response[T]] = {
    responseMonad.map(responseMonad.handleError(delegate.send(request)) {
      case e: Exception =>
        logger.error(s"Exception when sending request: $request", e)
        responseMonad.error(e)
    }) { response =>
      logger.debug(s"For request: $request, got response: $response")
      response
    }
  }
  override def openWebsocket[T, WS_RESULT](request: Request[T, S], handler: WS_HANDLER[WS_RESULT]): F[WebSocketResponse[WS_RESULT]] = {
    responseMonad.map(responseMonad.handleError(delegate.openWebsocket(request, handler)) {
      case e: Exception =>
        logger.error(s"Exception when opening websocket: $request", e)
        responseMonad.error(e)
    }) { response =>
      logger.debug(s"Websocket open: $request, with response headers: ${response.headers}")
      response
    }
  }
  override def close(): F[Unit] = delegate.close()
  override def responseMonad: MonadError[F] = delegate.responseMonad
}
