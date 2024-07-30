package com.softwaremill.bootzooka.http

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import com.softwaremill.bootzooka.*
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.util.{Id, SecureRandomId}
import com.softwaremill.tagging.*
import sttp.model.StatusCode
import sttp.tapir.Codec.PlainCodec
import sttp.tapir.json.jsoniter.TapirJsonJsoniter
import sttp.tapir.{Codec, Endpoint, EndpointOutput, PublicEndpoint, Schema, SchemaType, Tapir}

/** Helper class for defining HTTP endpoints. Import the members of this class when defining an HTTP API using tapir. */
class Http extends Tapir with TapirJsonJsoniter with TapirSchemas with Logging:

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

  val jsonErrorOutOutput: EndpointOutput[Error_OUT] = jsonBody[Error_OUT]

  /** Description of the output, that is used to represent an error that occurred during endpoint invocation. */
  // TODO
  val failOutput: EndpointOutput[Fail] =
    statusCode.and(jsonErrorOutOutput.map(_.error)(Error_OUT.apply)).map((_, _) => ???)(failToResponseData)

  /** Base endpoint description for non-secured endpoints. Specifies that errors are always returned as JSON values corresponding to the
    * [[Error_OUT]] class.
    */
  val baseEndpoint: PublicEndpoint[Unit, Fail, Unit, Any] =
    endpoint
      .errorOut(failOutput)
      // Prevent clickjacking attacks: https://cheatsheetseries.owasp.org/cheatsheets/Clickjacking_Defense_Cheat_Sheet.html
      .out(header("X-Frame-Options", "DENY"))
      .out(header("Content-Security-Policy", "frame-ancestors 'none'"))

  /** Base endpoint description for secured endpoints. Specifies that errors are always returned as JSON values corresponding to the
    * [[Error_OUT]] class, and that authentication is read from the `Authorization: Bearer` header.
    */
  val secureEndpoint: Endpoint[Id, Unit, Fail, Unit, Any] =
    baseEndpoint.securityIn(auth.bearer[String]().map(_.asInstanceOf[Id])(identity))
end Http

/** Schemas for types used in endpoint descriptions (as parts of query parameters, JSON bodies, etc.). Includes explicitly defined schemas
  * for custom types, and auto-derivation for ADTs & value classes.
  */
trait TapirSchemas:
  given PlainCodec[SecureRandomId] = Codec.string.map(_.asInstanceOf[SecureRandomId])(identity)
  given taggedPlainCodec[U, T](using uc: PlainCodec[U]): PlainCodec[U @@ T] =
    uc.map(_.taggedWith[T])(identity)

  given Schema[Id] = Schema(SchemaType.SString[Id]())
  given schemaForTagged[U, T](using uc: Schema[U]): Schema[U @@ T] = uc.asInstanceOf[Schema[U @@ T]]

case class Error_OUT(error: String) derives ConfiguredJsonValueCodec, Schema
