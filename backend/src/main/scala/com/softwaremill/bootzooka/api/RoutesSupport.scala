package com.softwaremill.bootzooka.api

import java.util.UUID

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.CacheDirectives.{`max-age`, `must-revalidate`, `no-cache`, `no-store`, `public`}
import akka.http.scaladsl.model.headers.{`Cache-Control`, `Expires`, `Last-Modified`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive1}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import com.softwaremill.bootzooka.user.{Session, UserJson, UserService}
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.{RememberMeStorage, SessionManager}
import cats.data.Xor
import io.circe._
import io.circe.jawn.decode

import scala.concurrent.ExecutionContext
trait RoutesSupport extends JsonSupport with SessionSupport {
  def completeOk = complete("ok")
}

trait JsonSupport extends CirceSupport {

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

  implicit def circeMarshaller[A <: AnyRef](implicit e: Encoder[A]): ToEntityMarshaller[A] = {
    Marshaller.StringMarshaller.wrap(ContentTypes.`application/json`) {
      e(_).noSpaces
    }
  }
}

trait SessionSupport {

  implicit def sessionManager: SessionManager[Session]
  implicit def rememberMeStorage: RememberMeStorage[Session]
  implicit def ec: ExecutionContext

  def userService: UserService

  def userFromSession: Directive1[UserJson] = userIdFromSession.flatMap { userId =>
    onSuccess(userService.findById(userId)).flatMap {
      case None => reject(AuthorizationFailedRejection)
      case Some(user) => provide(user)
    }
  }

  def userIdFromSession: Directive1[UUID] = persistentSession().flatMap {
    _.toOption match {
      case None => reject(AuthorizationFailedRejection)
      case Some(s) => provide(s.userId)
    }
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

  private def extensionTest(ext: String) = pathSuffixTest((".*\\." + ext + "$").r)

  val cacheImages =
    (extensionTest("png") | extensionTest("svg") | extensionTest("gif") | extensionTest("woff") | extensionTest("jpg")).flatMap { _ => cacheResponse } |
      doNotCacheResponse
}