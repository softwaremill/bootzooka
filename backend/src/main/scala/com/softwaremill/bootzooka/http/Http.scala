package com.softwaremill.bootzooka.http

import cats.effect.IO
import cats.implicits._
import com.softwaremill.bootzooka._
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.util.Id
import com.softwaremill.tagging._
import com.typesafe.scalalogging.StrictLogging
import io.circe.Printer
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.json.circe.TapirJsonCirce
import sttp.model.StatusCode
import sttp.tapir.{Codec, Endpoint, EndpointOutput, Schema, SchemaType, Tapir}
import sttp.tapir.generic.auto._
import tsec.common.SecureRandomId

/** Helper class for defining HTTP endpoints. Import the members of this class when defining an HTTP API using tapir.
  */
class Http() extends Tapir with TapirJsonCirce with TapirSchemas with StrictLogging {

  /** Description of the output, that is used to represent an error that occurred during endpoint invocation.
    */
  val failOutput: EndpointOutput[(StatusCode, Error_OUT)] = statusCode and jsonBody[Error_OUT]

  /** Base endpoint description for non-secured endpoints. Specifies that errors are always returned as JSON values corresponding to the
    * [[Error_OUT]] class.
    */
  val baseEndpoint: Endpoint[Unit, Unit, (StatusCode, Error_OUT), Unit, Any] =
    endpoint.errorOut(failOutput)

  /** Base endpoint description for secured endpoints. Specifies that errors are always returned as JSON values corresponding to the
    * [[Error_OUT]] class, and that authentication is read from the `Authorization: Bearer` header.
    */
  val secureEndpoint: Endpoint[Unit, Id, (StatusCode, Error_OUT), Unit, Any] =
    baseEndpoint.in(auth.bearer[String]().map(_.asInstanceOf[Id])(identity))

  //

  private val InternalServerError = (StatusCode.InternalServerError, "Internal server error")
  private val failToResponseData: Fail => (StatusCode, String) = {
    case Fail.NotFound(what)      => (StatusCode.NotFound, what)
    case Fail.Conflict(msg)       => (StatusCode.Conflict, msg)
    case Fail.IncorrectInput(msg) => (StatusCode.BadRequest, msg)
    case Fail.Forbidden           => (StatusCode.Forbidden, "Forbidden")
    case Fail.Unauthorized(msg)   => (StatusCode.Unauthorized, msg)
    case _                        => InternalServerError
  }

  def exceptionToErrorOut(e: Exception): (StatusCode, Error_OUT) = {
    val (statusCode, message) = e match {
      case f: Fail => failToResponseData(f)
      case _ =>
        logger.error("Exception when processing request", e)
        InternalServerError
    }

    logger.warn(s"Request fail: $message")
    val errorOut = Error_OUT(message)
    (statusCode, errorOut)
  }

  //

  implicit class TaskOut[T](f: IO[T]) {

    /** An extension method for [[IO]], which converts a possibly failed task, to a task which either returns the error converted to an
      * [[Error_OUT]] instance, or returns the successful value unchanged.
      */
    def toOut: IO[Either[(StatusCode, Error_OUT), T]] = {
      f.map(t => t.asRight[(StatusCode, Error_OUT)]).recover { case e: Exception =>
        exceptionToErrorOut(e).asLeft[T]
      }
    }
  }

  override def jsonPrinter: Printer = noNullsPrinter
}

/** Schemas for custom types used in endpoint descriptions (as parts of query parameters, JSON bodies, etc.)
  */
trait TapirSchemas {
  implicit val idPlainCodec: PlainCodec[SecureRandomId] =
    Codec.string.map(_.asInstanceOf[SecureRandomId])(identity)
  implicit def taggedPlainCodec[U, T](implicit uc: PlainCodec[U]): PlainCodec[U @@ T] =
    uc.map(_.taggedWith[T])(identity)

  implicit val schemaForId: Schema[Id] = Schema(SchemaType.SString[Id]())
  implicit def schemaForTagged[U, T](implicit uc: Schema[U]): Schema[U @@ T] = uc.asInstanceOf[Schema[U @@ T]]
}

case class Error_OUT(error: String)
