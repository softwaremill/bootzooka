package com.softwaremill.bootzooka.util

import sttp.shared.Identity
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

type Endpoints = List[AnyEndpoint]
type ServerEndpoints = List[ServerEndpoint[Any, Identity]]
