package com.softwaremill.bootzooka.metrics

import com.softwaremill.bootzooka.infrastructure.{Error_OUT, Http}
import com.softwaremill.bootzooka.infrastructure.Json._
import com.softwaremill.bootzooka.version.BuildInfo
import monix.eval.Task
import tapir.model.StatusCode
import tapir.server.ServerEndpoint

class VersionApi(http: Http) {
  import VersionApi._
  import http._

  val versionEndpoint: ServerEndpoint[Unit, (StatusCode, Error_OUT), Version_OUT, Nothing, Task] = baseEndpoint.get
    .in("version")
    .out(jsonBody[Version_OUT])
    .serverLogic { _ =>
      Task.now(Version_OUT(BuildInfo.builtAtString, BuildInfo.lastCommitHash)).toOut
    }
}

object VersionApi {
  case class Version_OUT(buildDate: String, buildSha: String)
}
