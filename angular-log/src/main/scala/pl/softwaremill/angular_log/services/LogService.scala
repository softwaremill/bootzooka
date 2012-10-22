package pl.softwaremill.angular_log.services

import javax.ws.rs._
import pl.softwaremill.angular_log.entities.LogObject
import java.util.Date
import scala.collection.JavaConversions._
import org.joda.time.DateTime

@Path("/logs")
class LogService {

  @GET
  @Produces(Array("application/json"))
  def getLogs():java.util.List[LogObject] = {
    println("Get logs");
    val javaList: java.util.List[LogObject] = Entries.list
    javaList
  }

  @GET
  @Path("/logs-count")
  @Produces(Array("application/json"))
  def getLogsCount() = {
     new LongResponseWrapper(Entries.list.size)
  }

  @POST
  @Consumes(Array("application/json"))
  def addNew(entry: LogObject) = {
    Entries.list = Entries.list.::(entry)
    println("added new " + entry.toString)
  }

}

object Entries {

  var list: List[LogObject] = List(
    new LogObject("Still hot, the latest tweet", new DateTime().toString("HH:mm dd/MM/yyyy"), "Teofilia Chrust"),
    new LogObject("This is really long tweet. This is really long tweet. This is really long tweet. " +
      "This is really long tweet. This is really long tweet. This is really long tweet.",
      new DateTime().withDate(2012, 8, 29).toString("HH:mm dd/MM/yyyy"), "Remigiusz Niemowa"),
    new LogObject("Nothing less, nothing more, just second tweet",
      new DateTime().withDate(2012, 6, 4).toString("HH:mm dd/MM/yyyy"), "Krystian Krostka"),
    new LogObject("Hello World! First entry",
      new DateTime().withDate(2012, 5, 20).toString("HH:mm dd/MM/yyyy"), "Jan Kowalski")
  )



}
