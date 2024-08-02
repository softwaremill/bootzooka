package com.softwaremill.bootzooka.util

import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint

type ServerEndpoints = List[ServerEndpoint[Any, Identity]]
