package com.softwaremill.bootzooka.test

import cats.data.OptionT
import cats.effect.Sync
import com.softwaremill.bootzooka.MainModule
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.infrastructure.Error_OUT
import com.softwaremill.bootzooka.infrastructure.Json._
import doobie.free.connection.ConnectionIO
import io.circe.{Decoder, Encoder}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.http4s.Credentials.Token
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, EntityEncoder, Headers, Request, Response, Status}
import org.scalatest.Matchers._

import scala.concurrent.duration._
import scala.reflect.ClassTag

trait HttpTestSupport extends Http4sDsl[Task] {

  val modules: MainModule

  implicit def entityEncoderFromCirce[F[_]: Sync, T: Encoder]: EntityEncoder[F, T] = {
    org.http4s.circe.jsonEncoderWithPrinterOf[F, T](noNullsPrinter)
  }

  implicit def entityDecoderFromCirce[F[_]: Sync, T: Decoder]: EntityDecoder[F, T] = {
    org.http4s.circe.jsonOf[F, T]
  }

  implicit class RichTask[T](t: Task[T]) {
    def unwrap: T = t.runSyncUnsafe(1.minute)
  }

  implicit class RichConnectionIO[T](t: ConnectionIO[T]) {
    def unwrap: T = t.transact(modules.xa).unwrap
  }

  def eventuallyTask[T](t: Task[T]): Task[T] = t.onErrorRestartLoop(100) { (err, maxRetries, retry) =>
    if (maxRetries > 0)
      retry(maxRetries - 1).delayExecution(100.milliseconds)
    else
      Task.raiseError(err)
  }

  implicit class RichOptionTResponse(t: OptionT[Task, Response[Task]]) {
    def unwrap: Response[Task] = t.value.unwrap match {
      case None    => fail("No response!")
      case Some(r) => r
    }
  }

  implicit class RichResponse(r: Response[Task]) {
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

  def authorizedRequest(token: String, request: Request[Task]): Request[Task] = {
    val authHeader = Authorization(Token(CaseInsensitiveString("Bearer"), token))
    request.withHeaders(request.headers ++ Headers.of(authHeader))
  }

  def responseBodyShouldBeEmpty(r: Response[Task]): Unit = {
    r.body.compile.toVector.unwrap.isEmpty shouldBe true
    ()
  }

  def responseBody(r: Response[Task]): String = {
    new String(r.body.compile.toVector.unwrap.toArray)
  }
}
