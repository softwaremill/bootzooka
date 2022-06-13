package com.softwaremill.bootzooka.metrics

import cats.effect.IO
import com.softwaremill.bootzooka.http.Http
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.version.BuildInfo
import sttp.tapir.server.ServerEndpoint

/** Defines an endpoint which exposes the current application version information.
  */
class VersionApi(http: Http) {
  import VersionApi._
  import http._

  val versionEndpoint: ServerEndpoint[Any, IO] = baseEndpoint.get
    .in("version")
    .out(jsonBody[Version_OUT])
    .serverLogic { _ =>
      IO(Version_OUT(BuildInfo.lastCommitHash)).toOut
    }
}

object VersionApi {
  case class Version_OUT(buildSha: String)
}
