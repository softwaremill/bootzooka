package com.softwaremill.bootzooka.util

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import com.softwaremill.bootzooka.util.CorrelationIdDecorator.CorrelationIdSource
import org.http4s.Request
import org.typelevel.ci.CIString

// TODO: use the correlator project once it's updated
class Http4sCorrelationMiddleware(correlationId: CorrelationIdDecorator) {

  def withCorrelationId[T, R](
      service: Kleisli[OptionT[IO, *], T, R]
  )(implicit source: CorrelationIdSource[T]): Kleisli[OptionT[IO, *], T, R] = {
    val runOptionT: T => IO[Option[R]] = service.run.andThen(_.value)
    Kleisli(correlationId.withCorrelationId[T, Option[R]](runOptionT).andThen(OptionT.apply))
  }
}

object Http4sCorrelationMiddleware {
  def apply(correlationId: CorrelationIdDecorator): Http4sCorrelationMiddleware = new Http4sCorrelationMiddleware(correlationId)

  val HeaderName: String = "X-Correlation-ID"

  implicit val source: CorrelationIdSource[Request[IO]] = (t: Request[IO]) => {
    t.headers.get(CIString(HeaderName)).map(_.toString())
  }
}
