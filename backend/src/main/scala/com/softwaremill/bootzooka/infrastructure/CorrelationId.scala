package com.softwaremill.bootzooka.infrastructure

import cats.data.Kleisli
import cats.effect.{IO, IOLocal}
import com.softwaremill.bootzooka.infrastructure.Doobie.{KleisliInterpreter, Transactor}
import doobie.WeakAsync
import doobie.free.connection.ConnectionIO
import sttp.client3._
import sttp.client3.{Response, SttpBackend}
import sttp.capabilities.Effect
import sttp.monad.MonadError
import sttp.tapir.server.interceptor.{EndpointInterceptor, RequestHandler, RequestInterceptor, Responder}

import scala.util.Random

object CorrelationId {
  private val localCid = {
    import cats.effect.unsafe.implicits.global
    IOLocal(None: Option[String]).unsafeRunSync()
  }

  private val random = new Random()
  private def generate(): String = {
    def randomUpperCaseChar() = (random.nextInt(91 - 65) + 65).toChar
    def segment = (1 to 3).map(_ => randomUpperCaseChar()).mkString
    s"$segment-$segment-$segment"
  }

  def get: IO[Option[String]] = localCid.get
  def set(v: Option[String]): IO[Unit] = localCid.set(v)
  def setOrNew(v: Option[String]): IO[Unit] = localCid.set(Some(v.getOrElse(generate())))

  /** Hacking doobie: an function which we'll use as a parameter to `Raw` to signal that the current correlation id should be returned
    * instead.
    */
  private val getCidMarker: java.sql.Connection => Option[String] = _ => ???

  /** Returns a [[ConnectionIO]] which reads the current correlation id. Requires a transactor wrapped with [[correlationIdTransactor]] to
    * function properly.
    */
  def getConnectionIO: ConnectionIO[Option[String]] = doobie.free.connection.raw(getCidMarker)

  /** A transactor wrapper, which properly interprets an instruction to get the current correlation id, created with [[getConnectionIO]]. */
  def correlationIdTransactor(delegate: Transactor[IO]): Transactor[IO] = delegate.copy(interpret0 = {
    new KleisliInterpreter[IO] {
      override implicit val asyncM: WeakAsync[IO] = WeakAsync.doobieWeakAsyncForAsync(IO.asyncForIO)

      override def raw[J, A](f: J => A): Kleisli[IO, J, A] =
        if (f eq getCidMarker) Kleisli((_: java.sql.Connection) => get).asInstanceOf[Kleisli[IO, J, A]] else super.raw(f)
    }.ConnectionInterpreter
  })
}

// covariance improves type inference, see: https://groups.google.com/g/scala-language/c/dQEomVCH3CI
trait CorrelationIdSource[+F[_]] {
  def get: F[Option[String]]
  def map[T](f: Option[String] => T): F[T]
}

object CorrelationIdSource {
  implicit val forIO: CorrelationIdSource[IO] = new CorrelationIdSource[IO] {
    override def get: IO[Option[String]] = CorrelationId.get
    override def map[T](f: Option[String] => T): IO[T] = get.map(f)
  }

  implicit val forConnectionIO: CorrelationIdSource[ConnectionIO] = new CorrelationIdSource[ConnectionIO] {
    override def get: ConnectionIO[Option[String]] = CorrelationId.getConnectionIO
    override def map[T](f: Option[String] => T): ConnectionIO[T] = get.map(f)
  }
}

/** An sttp backend wrapper, which sets the current correlation id on all outgoing requests. */
class SetCorrelationIdBackend[P](delegate: SttpBackend[IO, P]) extends SttpBackend[IO, P] {
  override def send[T, R >: P with Effect[IO]](request: Request[T, R]): IO[Response[T]] = {
    CorrelationId.get
      .map {
        case Some(cid) => request.header(CorrelationIdInterceptor.HeaderName, cid)
        case None      => request
      }
      .flatMap(delegate.send)
  }

  override def close(): IO[Unit] = delegate.close()

  override def responseMonad: MonadError[IO] = delegate.responseMonad
}

/** A tapir interceptor, which reads the correlation id from the headers; if it's absent, generates a new one. */
object CorrelationIdInterceptor extends RequestInterceptor[IO] {
  val HeaderName: String = "X-Correlation-ID"

  override def apply[R, B](
      responder: Responder[IO, B],
      requestHandler: EndpointInterceptor[IO] => RequestHandler[IO, R, B]
  ): RequestHandler[IO, R, B] =
    RequestHandler.from { case (request, endpoints, monad) =>
      val set = CorrelationId.setOrNew(request.header(HeaderName))
      set >> requestHandler(EndpointInterceptor.noop)(request, endpoints)(monad)
    }
}
