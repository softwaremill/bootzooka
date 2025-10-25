package com.softwaremill.bootzooka.http

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import com.softwaremill.bootzooka.*
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.util.Strings.{Id, asId}
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.jsoniter.*

/** Common definitions used when defining HTTP endpoints. */
object Http extends Logging:
  private val internalServerError = (StatusCode.InternalServerError, "Internal server error")
  private val failToResponseData: Fail => (StatusCode, String) = {
    case Fail.NotFound(what)      => (StatusCode.NotFound, what)
    case Fail.Conflict(msg)       => (StatusCode.Conflict, msg)
    case Fail.IncorrectInput(msg) => (StatusCode.BadRequest, msg)
    case Fail.Forbidden           => (StatusCode.Forbidden, "Forbidden")
    case Fail.Unauthorized(msg)   => (StatusCode.Unauthorized, msg)
    case _                        => internalServerError
  }
  // inverse of failToResponseData, used in tests to parse the response data into a Fail instance
  private val responseDataToFail: (StatusCode, String) => Fail = {
    case (StatusCode.NotFound, what)    => Fail.NotFound(what)
    case (StatusCode.Conflict, msg)     => Fail.Conflict(msg)
    case (StatusCode.BadRequest, msg)   => Fail.IncorrectInput(msg)
    case (StatusCode.Forbidden, _)      => Fail.Forbidden
    case (StatusCode.Unauthorized, msg) => Fail.Unauthorized(msg)
    case (code, msg)                    =>
      new Fail:
        override def toString = msg
  }

  //

  val jsonErrorOutOutput: EndpointOutput[Error_OUT] = jsonBody[Error_OUT]

  /** Description of the output, that is used to represent an error that occurred during endpoint invocation. */
  private val failOutput: EndpointOutput[Fail] =
    statusCode
      .and(jsonErrorOutOutput.map(_.error)(Error_OUT.apply))
      .map(responseDataToFail.tupled)(failToResponseData)

  /** Base endpoint description for non-secured endpoints. Specifies that errors are always returned as JSON values corresponding to the
    * [[Error_OUT]] class, translated from a [[Fail]] instance.
    */
  val baseEndpoint: PublicEndpoint[Unit, Fail, Unit, Any] =
    endpoint
      .errorOut(failOutput)
      // Prevent clickjacking attacks: https://cheatsheetseries.owasp.org/cheatsheets/Clickjacking_Defense_Cheat_Sheet.html
      .out(header("X-Frame-Options", "DENY"))
      .out(header("Content-Security-Policy", "frame-ancestors 'none'"))

  /** Base endpoint description for secured endpoints. Specifies that errors are always returned as JSON values corresponding to the
    * [[Error_OUT]] class, translated from a [[Fail]] instance, and that authentication is read from the `Authorization: Bearer` header.
    */
  def secureEndpoint[T]: Endpoint[Id[T], Unit, Fail, Unit, Any] =
    baseEndpoint.securityIn(auth.bearer[String]().map(_.asId[T])(_.toString))
end Http

case class Error_OUT(error: String) derives ConfiguredJsonValueCodec, Schema
