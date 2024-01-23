package com.softwaremill.bootzooka.passkeys

import cats.data.NonEmptyList
import cats.effect.IO
import com.softwaremill.bootzooka.http.Http
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.security.{ApiKey, Auth}
import com.softwaremill.bootzooka.util.ServerEndpoints
import doobie.util.transactor.Transactor

class PasskeysApi (http: Http, auth: Auth[ApiKey], passkeysService: PasskeysService, xa: Transactor[IO]) {
  import PasskeysApi._
  import http._

  private val PasskeysPath = "passkey"

  private val authedEndpoint = secureEndpoint.serverSecurityLogic(authData => auth(authData).toOut)

  private val registerPasskeyEndpoint = authedEndpoint.post
    .in(PasskeysPath / "registerpasskey")
    .in(header[String]("Origin"))
    .in(jsonBody[RegisterPasskey_IN])
    .out(jsonBody[RegisterPasskey_OUT])
    .serverLogic(id => {
        case (origin, data) =>
          (for {
            _ <- passkeysService.registerPasskey(id, origin, data.attestationObject, data.clientDataJSON,
                                              data.clientExtensionJSON, data.transports).transact(xa)
          } yield RegisterPasskey_OUT()).toOut
      }
    )

    val endpoints: ServerEndpoints =
        NonEmptyList
        .of(
            registerPasskeyEndpoint
        )
        .map(_.tag("passkey"))
}

object PasskeysApi {
  case class RegisterPasskey_IN(attestationObject: Array[Int], clientDataJSON: Array[Int],
                                clientExtensionJSON: String, transports: List[String])

  case class RegisterPasskey_OUT()
}