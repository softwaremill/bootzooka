package com.softwaremill.bootzooka.infrastructure

import ox.ForkLocal
import sttp.client3.*
import sttp.client3.{Response, SttpBackend}
import sttp.capabilities.Effect
import sttp.monad.MonadError
import sttp.tapir.server.interceptor.{EndpointInterceptor, RequestHandler, RequestInterceptor, Responder}

import scala.util.Random

object CorrelationId:
  val forkLocal: ForkLocal[Option[String]] = ForkLocal(None)

  private val random = new Random()
  def generate(): String =
    def randomUpperCaseChar() = (random.nextInt(91 - 65) + 65).toChar
    def segment = (1 to 3).map(_ => randomUpperCaseChar()).mkString
    s"$segment-$segment-$segment"

/** An sttp backend wrapper, which sets the current correlation id on all outgoing requests. */
class SetCorrelationIdBackend[P](delegate: SttpBackend[Identity, P]) extends SttpBackend[Identity, P]:
  override def send[T, R >: P with Effect[Identity]](request: Request[T, R]): Response[T] =
    val request2 = CorrelationId.forkLocal.get() match {
      case Some(cid) => request.header(CorrelationIdInterceptor.HeaderName, cid)
      case None      => request
    }
    delegate.send(request2)

  override def close(): Unit = delegate.close()
  override def responseMonad: MonadError[Identity] = delegate.responseMonad

/** A tapir interceptor, which reads the correlation id from the headers, or if it's absent, generates a new one. The correlation id is set
  * for the duration of processing the request.
  */
object CorrelationIdInterceptor extends RequestInterceptor[Identity]:
  val HeaderName: String = "X-Correlation-ID"

  override def apply[R, B](
      responder: Responder[Identity, B],
      requestHandler: EndpointInterceptor[Identity] => RequestHandler[Identity, R, B]
  ): RequestHandler[Identity, R, B] =
    RequestHandler.from { case (request, endpoints, monad) =>
      val cid = request.header(HeaderName).getOrElse(CorrelationId.generate())
      CorrelationId.forkLocal.supervisedWhere(Some(cid)) {
        requestHandler(EndpointInterceptor.noop)(request, endpoints)(monad)
      }
    }
