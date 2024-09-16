package com.softwaremill.bootzooka.admin

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import com.softwaremill.bootzooka.http.Http.*
import com.softwaremill.bootzooka.version.BuildInfo
import sttp.shared.Identity
import sttp.tapir.Schema
import sttp.tapir.server.ServerEndpoint
import com.softwaremill.bootzooka.http.EndpointsForDocs
import com.softwaremill.bootzooka.http.ServerEndpoints

/** Defines an endpoint which exposes the current application version information. */
class VersionApi extends ServerEndpoints:
  import VersionApi._

  private val versionServerEndpoint: ServerEndpoint[Any, Identity] = versionEndpoint.handleSuccess { _ =>
    Version_OUT(BuildInfo.lastCommitHash)
  }

  override val endpoints = List(versionServerEndpoint)

object VersionApi extends EndpointsForDocs:
  private val AdminPath = "admin"

  private val versionEndpoint = baseEndpoint.get
    .in(AdminPath / "version")
    .out(jsonBody[Version_OUT])

  override val endpointsForDocs = List(versionEndpoint).map(_.tag("admin"))

  case class Version_OUT(buildSha: String) derives ConfiguredJsonValueCodec, Schema
