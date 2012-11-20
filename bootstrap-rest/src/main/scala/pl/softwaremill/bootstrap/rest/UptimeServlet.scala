package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.common.JsonWrapper
import org.joda.time.{Duration, DateTime}

class UptimeServlet(serverStartDate: DateTime) extends BootstrapServlet {

  get("/") {
    JsonWrapper(new Duration(serverStartDate, new DateTime()).getStandardSeconds)
  }

}
