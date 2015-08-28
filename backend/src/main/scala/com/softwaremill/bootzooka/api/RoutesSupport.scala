package com.softwaremill.bootzooka.api

import java.util.UUID

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.CacheDirectives.{`no-cache`, `no-store`, `must-revalidate`, `max-age`, `public`}
import akka.http.scaladsl.model.headers.{`Last-Modified`, `Cache-Control`, `Expires`}
import akka.http.scaladsl.server.{Directive1, AuthorizationFailedRejection}
import akka.http.scaladsl.unmarshalling.{Unmarshaller, FromEntityUnmarshaller}
import akka.stream.Materializer
import com.softwaremill.bootzooka.user.{UserJson, Session, UserService}
import com.softwaremill.session.{RememberMeStorage, SessionManager}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.json4s._
import org.json4s.JsonAST.JString
import akka.http.scaladsl.server.Directives._
import com.softwaremill.session.SessionDirectives._

import scala.concurrent.ExecutionContext

trait RoutesSupport extends JsonSupport with SessionSupport {
  def completeOk = complete("ok")
}

trait JsonSupport {
  protected val dateTimeFormat = ISODateTimeFormat.basicDateTime()
  protected val dateTimeSerializer = new CustomSerializer[DateTime](formats => ({
    case JString(s) => dateTimeFormat.parseDateTime(s)
  }, {
    case d: DateTime => JString(dateTimeFormat.print(d))
  }))

  protected implicit def jsonFormats: Formats = DefaultFormats + dateTimeSerializer

  implicit val serialization = native.Serialization
  implicit def materializer: Materializer

  // from https://github.com/hseeberger/akka-http-json/blob/master/akka-http-json4s/src/main/scala/de/heikoseeberger/akkahttpjson4s/Json4sSupport.scala
  implicit def json4sUnmarshaller[A <: Product: Manifest]: FromEntityUnmarshaller[A] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(MediaTypes.`application/json`)
      .mapWithCharset { (data, charset) =>
        val input = if (charset == HttpCharsets.`UTF-8`) data.utf8String else data.decodeString(charset.nioCharset.name)
        serialization.read(input)
      }

  implicit def json4sMarshaller[A <: AnyRef](implicit cbs: CanBeSerialized[A]): ToEntityMarshaller[A] = {
    import native.JsonMethods._
    Marshaller.StringMarshaller.wrap(ContentTypes.`application/json`) {
      (Extraction.decompose _)
        .andThen(render)
        .andThen(compact)
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