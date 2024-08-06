package com.softwaremill.bootzooka.infrastructure

import com.softwaremill.bootzooka.logging.Logging
import org.slf4j.MDC
import ox.logback.InheritableMDC
import sttp.client3.*
import sttp.client3.{Response, SttpBackend}
import sttp.capabilities.Effect
import sttp.monad.MonadError
import sttp.tapir.server.interceptor.{EndpointInterceptor, RequestHandler, RequestInterceptor, Responder}

import scala.util.Random

object CorrelationId:
  private val random = new Random()
  def generate(): String =
    def randomUpperCaseChar() = (random.nextInt(91 - 65) + 65).toChar
    def segment = (1 to 3).map(_ => randomUpperCaseChar()).mkString
    s"$segment-$segment-$segment"

/** An sttp backend wrapper, which sets the current correlation id (from [[CorrelationId.forkLocal]]) on all outgoing requests. */
class SetCorrelationIdBackend[P](delegate: SttpBackend[Identity, P]) extends SttpBackend[Identity, P]:
  override def send[T, R >: P with Effect[Identity]](request: Request[T, R]): Response[T] =
    val request2 = Option(MDC.get(CorrelationIdInterceptor.MDCKey)) match {
      case Some(cid) => request.header(CorrelationIdInterceptor.HeaderName, cid)
      case None      => request
    }
    delegate.send(request2)

  override def close(): Unit = delegate.close()
  override def responseMonad: MonadError[Identity] = delegate.responseMonad

/** A tapir interceptor, which reads the correlation id from the headers, or if it's absent, generates a new one. The correlation id is set
  * in Logback's MDC and in [[CorrelationId.forkLocal]] for the duration of processing the request.
  */
object CorrelationIdInterceptor extends RequestInterceptor[Identity] with Logging:
  val HeaderName: String = "X-Correlation-ID"
  val MDCKey = "cid"

  override def apply[R, B](
      responder: Responder[Identity, B],
      requestHandler: EndpointInterceptor[Identity] => RequestHandler[Identity, R, B]
  ): RequestHandler[Identity, R, B] =
    RequestHandler.from { case (request, endpoints, monad) =>
      val cid = request.header(HeaderName).getOrElse(CorrelationId.generate())
      InheritableMDC.unsupervisedWhere(MDCKey -> cid) {
        requestHandler(EndpointInterceptor.noop)(request, endpoints)(monad)
      }
    }
