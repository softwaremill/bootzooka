package com.softwaremill.bootzooka.utils.http

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.CacheDirectives._
import akka.http.scaladsl.model.headers.{`Cache-Control`, `Last-Modified`, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive1}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import cats.data.Xor
import `X-Content-Type-Options`.`nosniff`
import `X-Frame-Options`.`DENY`
import `X-XSS-Protection`.`1; mode=block`
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Route
import com.softwaremill.bootzooka.user.{BasicUserData, Session, UserId}
import com.softwaremill.bootzooka.utils.ActorPerRequestFactory
import com.softwaremill.bootzooka.utils.http.PerRequest._
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import com.softwaremill.session.{RefreshTokenStorage, SessionManager}
import io.circe._
import io.circe.jawn.decode
import com.softwaremill.bootzooka.user.worker.UserFinder

import scala.concurrent.{ExecutionContext, Promise}

trait RoutesSupport extends PerRequestSupport with JsonSupport with SessionSupport {
  def completeOk = complete("ok")
}

trait PerRequestSupport {
  self: JsonSupport =>

  implicit def system: ActorSystem
  implicit def ec: ExecutionContext

  def perRequest(factory: ActorPerRequestFactory, command: Command)(check: PartialFunction[Event, Route]) = {

    perRequestDirective(factory, command)(check orElse {
      case ok: JustOK => complete("ok")
      case forbidden: Forbidden => complete(StatusCodes.Forbidden, forbidden.msg)
      case conflict: Conflict => complete(StatusCodes.Conflict, conflict.msg)
      case bad: Bad => complete(StatusCodes.BadRequest, bad.msg)
      case RequestTimeout => complete(StatusCodes.GatewayTimeout, RequestTimeout.msg)
      case _ => complete(StatusCodes.InternalServerError)
    })
  }
  def perRequestDirective(factory: ActorPerRequestFactory, command: Command): Directive1[Event] = {
    val promise = Promise[Event]
    val target = system actorOf factory.props

    system actorOf Props(new PerRequest(promise, target, command))

    onSuccess(promise.future)
  }
}

trait JsonSupport extends CirceEncoders {

  implicit def materializer: Materializer

  implicit def circeUnmarshaller[A <: Product: Manifest](implicit d: Decoder[A]): FromEntityUnmarshaller[A] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset { (data, charset) =>
        val input = if (charset == HttpCharsets.`UTF-8`) data.utf8String else data.decodeString(charset.nioCharset.name)
        decode[A](input) match {
          case Xor.Right(obj) => obj
          case Xor.Left(failure) => throw new IllegalArgumentException(failure.getMessage, failure.getCause)
        }
      }

  implicit def circeMarshaller[A <: AnyRef](implicit e: Encoder[A], cbs: CanBeSerialized[A]): ToEntityMarshaller[A] = {
    Marshaller.StringMarshaller.wrap(MediaTypes.`application/json`) {
      e(_).noSpaces
    }
  }

  /**
   * To limit what data can be serialized to the client, only classes of type `T` for which an implicit
   * `CanBeSerialized[T]` value is in scope will be allowed. You only need to provide an implicit for the base value,
   * any containers like `List` or `Option` will be automatically supported.
   */
  trait CanBeSerialized[T]
  object CanBeSerialized {
    def apply[T] = new CanBeSerialized[T] {}
    implicit def listCanBeSerialized[T](implicit cbs: CanBeSerialized[T]): CanBeSerialized[List[T]] = null
    implicit def setCanBeSerialized[T](implicit cbs: CanBeSerialized[T]): CanBeSerialized[Set[T]] = null
    implicit def optionCanBeSerialized[T](implicit cbs: CanBeSerialized[T]): CanBeSerialized[Option[T]] = null
  }
}

trait SessionSupport {
  self: PerRequestSupport =>

  import UserFinder._

  implicit def sessionManager: SessionManager[Session]
  implicit def refreshTokenStorage: RefreshTokenStorage[Session]
  implicit def ec: ExecutionContext

  def userFinder: UserFinder

  def userFromSession: Directive1[BasicUserData] = userIdFromSession flatMap { userId =>
    perRequestDirective(userFinder, FindUser(userId)) flatMap {
      case UserFound(user) => provide(user)
      case _ => reject(AuthorizationFailedRejection)
    }
  }
  def userIdFromSession: Directive1[UserId] = session(refreshable, usingCookies) map (_.toOption) flatMap {
    case None => reject(AuthorizationFailedRejection)
    case Some(s) => provide(s.userId)
  }

}

trait CacheSupport {
  import akka.http.scaladsl.model.DateTime

  private val doNotCacheResponse = respondWithHeaders(
    `Last-Modified`(DateTime.now),
    `Expires`(DateTime.now),
    `Cache-Control`(`no-cache`, `no-store`, `must-revalidate`, `max-age`(0))
  )
  private val cacheSeconds = 60L * 60L * 24L * 30L
  private val cacheResponse = respondWithHeaders(
    `Expires`(DateTime(System.currentTimeMillis() + cacheSeconds * 1000L)),
    `Cache-Control`(`public`, `max-age`(cacheSeconds))
  )

  private def extensionTest(ext: String): Directive1[String] = pathSuffixTest((".*\\." + ext + "$").r)
  private def extensionsTest(exts: String*): Directive1[String] = exts.map(extensionTest).reduceLeft(_ | _)

  val cacheImages =
    extensionsTest("png", "svg", "gif", "woff", "jpg").flatMap { _ => cacheResponse } |
      doNotCacheResponse
}

trait SecuritySupport {
  val addSecurityHeaders = respondWithHeaders(
    `X-Frame-Options`(`DENY`),
    `X-Content-Type-Options`(`nosniff`),
    `X-XSS-Protection`(`1; mode=block`)
  )
}
