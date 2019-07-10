package com.softwaremill.bootzooka.version

import cats.data.NonEmptyList
import com.softwaremill.bootzooka.ServerEndpoints
import com.softwaremill.bootzooka.infrastructure.Http
import com.softwaremill.bootzooka.infrastructure.Json._
import monix.eval.Task

class VersionApi(http: Http) {
  import http._
  import VersionApi._

  private val versionEndpoint = baseEndpoint.get
    .in("version")
    .out(jsonBody[Version_OUT])
    .serverLogic { _ =>
      Task.now(Version_OUT(BuildInfo.builtAtString, BuildInfo.lastCommitHash)).toOut
    }

  val endpoints: ServerEndpoints = NonEmptyList.of(versionEndpoint)
}

object VersionApi {
  case class Version_OUT(buildDate: String, buildSha: String)
}
