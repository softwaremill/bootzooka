package com.softwaremill.bootzooka.metrics

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import com.softwaremill.bootzooka.http.Http
import com.softwaremill.bootzooka.version.BuildInfo
import sttp.shared.Identity
import sttp.tapir.Schema
import sttp.tapir.server.ServerEndpoint

/** Defines an endpoint which exposes the current application version information. */
class VersionApi(http: Http):
  import VersionApi._
  import http._

  val versionEndpoint: ServerEndpoint[Any, Identity] = baseEndpoint.get
    .in("version")
    .out(jsonBody[Version_OUT])
    .handleSuccess { _ => Version_OUT(BuildInfo.lastCommitHash) }

object VersionApi:
  case class Version_OUT(buildSha: String) derives ConfiguredJsonValueCodec, Schema
