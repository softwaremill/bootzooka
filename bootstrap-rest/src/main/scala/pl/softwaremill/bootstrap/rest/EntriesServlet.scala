package pl.softwaremill.bootstrap.rest

import org.scalatra.ScalatraServlet
import pl.softwaremill.bootstrap.service.EntryService
import pl.softwaremill.bootstrap.domain.Entry
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s.{DefaultFormats, Formats}
import pl.softwaremill.bootstrap.common.{SafeInt, JsonWrapper}

class EntriesServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult {

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  get("/:id") {
    SafeInt(params("id")) match {
      case id: Option[Int] =>
        EntryService.load(id.getOrElse(-1)) match {
          case entry: Entry => entry
          case _ => null
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
    val entry: Entry = parsedBody.extract[Entry]
    if(entry.id > 0) {
      EntryService.update(entry)
    }
    else {
      EntryService.add(entry)
    }

  }

  delete("/:id") {
    SafeInt(params("id")) match {
      case id: Option[Int] =>
        EntryService.remove(id.getOrElse(-1))
      case _ => null
    }
  }

}
