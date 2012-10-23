package pl.softwaremill.demo.scalate.resources

import javax.ws.rs._
import com.sun.jersey.api.view.ImplicitProduces
import core.{MediaType, Response}
import pl.softwaremill.demo.scalate.entities.LogObject
import scala.collection.JavaConversions._
import org.joda.time.DateTime
import java.net.URI
import javax.ws.rs.Path

@Path("/log")
@ImplicitProduces(Array(MediaType.TEXT_HTML))
class LogResource {

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  def logs(): java.util.List[LogObject] = {
    val javaList: java.util.List[LogObject] = Entries.list
    javaList
  }

  @POST
  def addNew(@FormParam("text") text: String, @FormParam("author") author: String): Response = {
    Entries.list = Entries.list.::(new LogObject(text, new DateTime().toString("HH:mm dd/MM/yyyy"), author))
    Response.seeOther(URI.create("/log")).build()
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

