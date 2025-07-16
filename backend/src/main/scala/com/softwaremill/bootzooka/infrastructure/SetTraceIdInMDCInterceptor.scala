package com.softwaremill.bootzooka.infrastructure

import com.softwaremill.bootzooka.logging.Logging
import io.opentelemetry.api.trace.Span
import ox.logback.InheritableMDC
import sttp.shared.Identity
import sttp.tapir.server.interceptor.{EndpointInterceptor, RequestHandler, RequestInterceptor, Responder}

/** A Tapir interceptor, which sets the current trace id in the MDC, so that the logs that are printed to the console can be easily
  * correlated as well. This interceptor should come after the OpenTelemetry tracing interceptor.
  */
object SetTraceIdInMDCInterceptor extends RequestInterceptor[Identity] with Logging:
  val MDCKey = "traceId"

  override def apply[R, B](
      responder: Responder[Identity, B],
      requestHandler: EndpointInterceptor[Identity] => RequestHandler[Identity, R, B]
  ): RequestHandler[Identity, R, B] =
    RequestHandler.from { case (request, endpoints, monad) =>
      val traceId = Span.current().getSpanContext().getTraceId()
      InheritableMDC.unsupervisedWhere(MDCKey -> traceId) {
        requestHandler(EndpointInterceptor.noop)(request, endpoints)(using monad)
      }
    }
