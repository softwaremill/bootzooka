package com.softwaremill.bootzooka.test

import cats.data.OptionT
import cats.effect.kernel.Concurrent
import cats.effect.{IO, Sync}
import com.softwaremill.bootzooka.MainModule
import com.softwaremill.bootzooka.http.Error_OUT
import com.softwaremill.bootzooka.infrastructure.Json._
import io.circe.{Decoder, Encoder}
import org.http4s.Credentials.Token
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.{EntityDecoder, EntityEncoder, Headers, Request, Response, Status}
import org.scalatest.matchers.should.Matchers
import org.typelevel.ci.CIString
import cats.effect.unsafe.implicits.global

import scala.concurrent.duration._
import scala.reflect.ClassTag

trait HttpTestSupport extends Http4sDsl[IO] with Matchers {

  val modules: MainModule

  // in tests we are using the http4s client, hence we need http4s entity encoders/decoders to send/receive data
  implicit def entityEncoderFromCirce[F[_]: Sync, T: Encoder]: EntityEncoder[F, T] = {
    org.http4s.circe.jsonEncoderWithPrinterOf[F, T](noNullsPrinter)
  }

  implicit def entityDecoderFromCirce[F[_]: Sync: Concurrent, T: Decoder]: EntityDecoder[F, T] = {
    org.http4s.circe.jsonOf[F, T]
  }

  implicit class RichTask[T](t: IO[T]) {
    def unwrap: T = t.unsafeRunTimed(1.minute).get
  }

  implicit class RichOptionTResponse(t: OptionT[IO, Response[IO]]) {
    def unwrap: Response[IO] = t.value.unwrap match {
      case None    => fail("No response!")
      case Some(r) => r
    }
  }

  implicit class RichResponse(r: Response[IO]) {
    def shouldDeserializeTo[T: Decoder: ClassTag]: T = {
      if (r.status != Status.Ok) {
        fail(s"Response status: ${r.status}: ${r.attemptAs[String].value.unwrap}")
      } else {
        val attemptResult = r.attemptAs[T].value.unwrap
        attemptResult match {
          case Left(df) => fail(s"Cannot deserialize to ${implicitly[ClassTag[T]].runtimeClass.getName}:\n$df")
          case Right(v) => v
        }
      }
    }

    def shouldDeserializeToError: String = {
      val attemptResult = r.attemptAs[Error_OUT].value.unwrap
      attemptResult match {
        case Left(df) => fail(s"Cannot deserialize to error:\n$df")
        case Right(v) => v.error
      }
    }
  }

  def authorizedRequest(token: String, request: Request[IO]): Request[IO] = {
    val authHeader = Authorization(Token(CIString("Bearer"), token))
    request.withHeaders(request.headers ++ Headers.apply(authHeader))
  }
}
