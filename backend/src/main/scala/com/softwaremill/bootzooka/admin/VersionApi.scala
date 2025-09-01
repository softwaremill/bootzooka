package com.softwaremill.bootzooka.admin

import com.github.plokhotnyuk.jsoniter_scala.macros.ConfiguredJsonValueCodec
import com.softwaremill.bootzooka.http.Http.*
import com.softwaremill.bootzooka.http.{EndpointsForDocs, ServerEndpoints}
import com.softwaremill.bootzooka.version.BuildInfo
import com.softwaremill.macwire.wireList
import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.json.jsoniter.*
import sttp.tapir.server.ServerEndpoint

/** Defines an endpoint which exposes the current application version information. */
class VersionApi extends ServerEndpoints:
  import VersionApi.*

  private val versionServerEndpoint: ServerEndpoint[Any, Identity] = versionEndpoint.handleSuccess { _ =>
    Version_OUT(BuildInfo.lastCommitHash)
  }

  override val endpoints = wireList
end VersionApi

object VersionApi extends EndpointsForDocs:
  private val AdminPath = "admin"

  private val versionEndpoint = baseEndpoint.get
    .in(AdminPath / "version")
    .out(jsonBody[Version_OUT])

  override val endpointsForDocs = wireList[AnyEndpoint].map(_.tag("admin"))

  case class Version_OUT(buildSha: String) derives ConfiguredJsonValueCodec, Schema
end VersionApi
