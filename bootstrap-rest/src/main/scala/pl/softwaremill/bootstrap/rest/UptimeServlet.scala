package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.common.{UptimeSupport, JsonWrapper}

class UptimeServlet extends JsonServlet with UptimeSupport {

  get("/") {
    JsonWrapper(serverUptime())
  }

}
