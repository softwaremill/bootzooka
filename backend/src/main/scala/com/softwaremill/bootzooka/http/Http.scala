package com.softwaremill.bootzooka.http

import cats.effect.IO
import cats.implicits._
import com.softwaremill.bootzooka._
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.logging.FLogging
import com.softwaremill.bootzooka.util.Id
import com.softwaremill.tagging._
import io.circe.Printer
import sttp.model.StatusCode
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.generic.auto.SchemaDerivation
import sttp.tapir.json.circe.TapirJsonCirce
import sttp.tapir.{Codec, Endpoint, EndpointOutput, PublicEndpoint, Schema, SchemaType, Tapir}
import tsec.common.SecureRandomId

/** Helper class for defining HTTP endpoints. Import the members of this class when defining an HTTP API using tapir. */
class Http() extends Tapir with TapirJsonCirce with TapirSchemas with FLogging {

  val jsonErrorOutOutput: EndpointOutput[Error_OUT] = jsonBody[Error_OUT]

  /** Description of the output, that is used to represent an error that occurred during endpoint invocation. */
  val failOutput: EndpointOutput[(StatusCode, Error_OUT)] = statusCode.and(jsonErrorOutOutput)

  /** Base endpoint description for non-secured endpoints. Specifies that errors are always returned as JSON values corresponding to the
    * [[Error_OUT]] class.
    */
  val baseEndpoint: PublicEndpoint[Unit, (StatusCode, Error_OUT), Unit, Any] =
    endpoint.errorOut(failOutput)

  /** Base endpoint description for secured endpoints. Specifies that errors are always returned as JSON values corresponding to the
    * [[Error_OUT]] class, and that authentication is read from the `Authorization: Bearer` header.
    */
  val secureEndpoint: Endpoint[Id, Unit, (StatusCode, Error_OUT), Unit, Any] =
    baseEndpoint.securityIn(auth.bearer[String]().map(_.asInstanceOf[Id])(identity))

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

  //

  implicit class IOOut[T](f: IO[T]) {

    /** An extension method for [[IO]], which converts a possibly failed IO, to one which either returns the error converted to an
      * [[Error_OUT]] instance, or returns the successful value unchanged.
      */
    def toOut: IO[Either[(StatusCode, Error_OUT), T]] = {
      f.map(t => t.asRight[(StatusCode, Error_OUT)]).recoverWith { case f: Fail =>
        val (statusCode, message) = failToResponseData(f)
        logger.warn[IO](s"Request fail: $message").map(_ => (statusCode, Error_OUT(message)).asLeft[T])
      }
    }
  }

  override def jsonPrinter: Printer = noNullsPrinter
}

/** Schemas for types used in endpoint descriptions (as parts of query parameters, JSON bodies, etc.). Includes explicitly defined schemas
  * for custom types, and auto-derivation for ADTs & value classes.
  */
trait TapirSchemas extends SchemaDerivation {
  implicit val idPlainCodec: PlainCodec[SecureRandomId] =
    Codec.string.map(_.asInstanceOf[SecureRandomId])(identity)
  implicit def taggedPlainCodec[U, T](implicit uc: PlainCodec[U]): PlainCodec[U @@ T] =
    uc.map(_.taggedWith[T])(identity)

  implicit val schemaForId: Schema[Id] = Schema(SchemaType.SString[Id]())
  implicit def schemaForTagged[U, T](implicit uc: Schema[U]): Schema[U @@ T] = uc.asInstanceOf[Schema[U @@ T]]
}

case class Error_OUT(error: String)
