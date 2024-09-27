package com.softwaremill.bootzooka.http

import sttp.shared.Identity
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

trait EndpointsForDocs:
  /** The list of endpoints which should appear in the generated OpenAPI description (used for docs and to generate frontend code).
    *
    * Usually, each endpoint should have the same [[sttp.tapir.Endpoint.tag]], and corresponds to exactly one endpoint defined in
    * [[ServerEndpoints.endpoints]].
    */
  def endpointsForDocs: List[AnyEndpoint]

trait ServerEndpoints:
  /** The list of server endpoints which should be exposed by the HTTP server. */
  def endpoints: List[ServerEndpoint[Any, Identity]]
