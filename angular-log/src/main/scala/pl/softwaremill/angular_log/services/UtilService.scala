package pl.softwaremill.angular_log.services

import javax.ws.rs._
import java.util.Date
import com.sun.jersey.api.view.ImplicitProduces
import util.parsing.json.JSON
import org.codehaus.jackson.annotate.JsonProperty


@Path("/uptime")
class UtilService() {

  @GET
  @Produces(Array("application/json"))
  def getUptime() = {
    println("getUptime")

    new LongResponseWrapper((new Date().getTime - UtilData.startDate.getTime)/1000)
  }

}

object UtilData {
  val startDate = new Date()
}
