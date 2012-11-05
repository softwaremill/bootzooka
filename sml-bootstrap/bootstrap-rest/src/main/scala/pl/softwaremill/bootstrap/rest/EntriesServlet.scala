package pl.softwaremill.bootstrap.rest

import org.scalatra.ScalatraServlet
import pl.softwaremill.bootstrap.service.EntryService
import pl.softwaremill.bootstrap.domain.Entry
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s.{DefaultFormats, Formats}
import pl.softwaremill.bootstrap.common.JsonWrapper

class EntriesServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult {

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  get("/:id") {
    SafeInt(params("id")) match {
      case id: Option[Int] =>
        EntryService.load(id.getOrElse(-1)) match {
          case entry: Some[Entry] =>
            entry
          case None =>
            null
        }
      case _ => null
    }
  }

  get("/") {
    EntryService.loadAll
  }

  get("/count") {
    JsonWrapper(EntryService.count)
  }

  post("/") {
    val newEntry: Entry = parsedBody.extract[Entry]
    EntryService.add(newEntry)
  }

  delete("/:id") {
    SafeInt(params("id")) match {
      case id: Option[Int] =>
        EntryService.remove(id.getOrElse(-1))
      case _ => null
    }
  }

}

object SafeInt {
  val IntPattern = "-?([0-9]+)"

  def apply(o: Option[String]): Option[Int] = apply(o.getOrElse(""))

  def apply(o: String): Option[Int] = if (o.matches(IntPattern)) Some(o.toInt) else None

  def unapply(o: String): Option[Int] = if (o.matches(IntPattern)) Some(o.toInt) else None

  def orNone(i: Any): Option[Int] = i match {
    case i: Int => Some(i)
    case l: Long => Some(l.toInt)
    case _ => None
  }
}