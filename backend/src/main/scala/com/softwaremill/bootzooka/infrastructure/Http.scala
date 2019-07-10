package com.softwaremill.bootzooka.infrastructure

import cats.implicits._
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka._
import com.softwaremill.tagging._
import com.typesafe.scalalogging.StrictLogging
import doobie.util.transactor.Transactor
import io.circe.Printer
import monix.eval.Task
import org.http4s.Request
import tapir.Codec.PlainCodec
import tapir.json.circe.TapirJsonCirce
import tapir.model.{StatusCode, StatusCodes}
import tapir.server.{DecodeFailureHandler, DecodeFailureHandling, ServerDefaults}
import tapir.{Codec, DecodeResult, Endpoint, EndpointOutput, Schema, SchemaFor, Tapir}
import tsec.common.SecureRandomId

class Http(val xa: Transactor[Task]) extends Tapir with TapirJsonCirce with TapirSchemas with StrictLogging {

  val failOutput: EndpointOutput[(StatusCode, Error_OUT)] = statusCode and jsonBody[Error_OUT]

  val baseEndpoint: Endpoint[Unit, (StatusCode, Error_OUT), Unit, Nothing] =
    endpoint.errorOut(failOutput)

  val secureEndpoint: Endpoint[Id, (StatusCode, Error_OUT), Unit, Nothing] =
    baseEndpoint.in(auth.bearer.map(_.asInstanceOf[Id])(identity))

  //

  private val failToResponseData: Fail => (StatusCode, String) = {
    case Fail.NotFound(what)      => (StatusCodes.NotFound, what)
    case Fail.IncorrectInput(msg) => (StatusCodes.BadRequest, msg)
    case Fail.Forbidden           => (StatusCodes.Forbidden, "Forbidden")
    case Fail.Unauthorized        => (StatusCodes.Unauthorized, "Unauthorized")
    case _                        => (StatusCodes.InternalServerError, "Internal server error")
  }

  private def failToErrorOut(f: Fail): (StatusCode, Error_OUT) = {
    val (statusCode, message) = failToResponseData(f)
    logger.warn(s"Request fail: $message")

    val errorOut = Error_OUT(message)
    (statusCode, errorOut)
  }

  //

  private def failResponse(code: StatusCode, msg: String): DecodeFailureHandling =
    DecodeFailureHandling.response(failOutput)((code, Error_OUT(msg)))

  val decodeFailureHandler: DecodeFailureHandler[Request[Task]] = {
    // if an exception is thrown when decoding an input, and the exception is a Fail, responding basing on the Fail
    case (_, _, DecodeResult.Error(_, f: Fail)) => DecodeFailureHandling.response(failOutput)(failToErrorOut(f))
    // otherwise, converting the decode input failure into a ParsingFailure response
    case (req, input, failure) =>
      ServerDefaults.decodeFailureHandlerUsingResponse(failResponse, badRequestOnPathFailureIfPathShapeMatches = false)(req, input, failure)
  }

  //

  implicit class TaskOut[T](f: Task[T]) {
    def toOut: Task[Either[(StatusCode, Error_OUT), T]] = {
      f.map(t => t.asRight[(StatusCode, Error_OUT)]).recover {
        case fail: Fail =>
          failToErrorOut(fail).asLeft[T]
      }
    }
  }

  implicit class ConnectionIOWrap[T](f: ConnectionIO[T]) {
    def transact: Task[T] = f.transact(xa)
  }

  override def jsonPrinter: Printer = noNullsPrinter
}

trait TapirSchemas {
  implicit val idPlainCodec: PlainCodec[SecureRandomId] =
    Codec.stringPlainCodecUtf8.map(_.asInstanceOf[SecureRandomId])(identity)
  implicit def taggedPlainCodec[U, T](implicit uc: PlainCodec[U]): PlainCodec[U @@ T] =
    uc.map(_.taggedWith[T])(identity)

  implicit val schemaForBigDecimal: SchemaFor[BigDecimal] = SchemaFor(Schema.SString)
  implicit val schemaForId: SchemaFor[Id] = SchemaFor(Schema.SString)
  implicit def schemaForTagged[U, T](implicit uc: SchemaFor[U]): SchemaFor[U @@ T] = uc.asInstanceOf[SchemaFor[U @@ T]]
}

case class Error_OUT(error: String)
