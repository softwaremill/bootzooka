package com.softwaremill.bootzooka.admin

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import com.softwaremill.bootzooka.http.Http.*
import com.softwaremill.bootzooka.util.{Endpoints, ServerEndpoints}
import com.softwaremill.bootzooka.version.BuildInfo
import sttp.shared.Identity
import sttp.tapir.Schema
import sttp.tapir.server.ServerEndpoint

/** Defines an endpoint which exposes the current application version information. */
class VersionApi:
  import VersionApi._

  private val versionServerEndpoint: ServerEndpoint[Any, Identity] = versionEndpoint.handleSuccess { _ =>
    Version_OUT(BuildInfo.lastCommitHash)
  }

  val serverEndpoints: ServerEndpoints = List(versionServerEndpoint)

object VersionApi:
  private val AdminPath = "admin"

  private val versionEndpoint = baseEndpoint.get
    .in(AdminPath / "version")
    .out(jsonBody[Version_OUT])

  val endpoints: Endpoints = List(versionEndpoint).map(_.tag("admin"))

  case class Version_OUT(buildSha: String) derives ConfiguredJsonValueCodec, Schema
