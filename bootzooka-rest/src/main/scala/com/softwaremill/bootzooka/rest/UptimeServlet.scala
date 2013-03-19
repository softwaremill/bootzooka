package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.common.{ UptimeSupport, JsonWrapper }

class UptimeServlet extends JsonServlet with UptimeSupport {

  get("/") {
    JsonWrapper(serverUptime())
  }

}
