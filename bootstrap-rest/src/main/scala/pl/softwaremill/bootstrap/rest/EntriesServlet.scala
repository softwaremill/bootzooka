package pl.softwaremill.bootstrap.rest

import org.scalatra.ScalatraServlet
import pl.softwaremill.bootstrap.service.EntryService
import pl.softwaremill.bootstrap.domain.Entry
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s.{DefaultFormats, Formats}
import pl.softwaremill.bootstrap.common.{SafeInt, JsonWrapper}
import pl.softwaremill.bootstrap.auth.AuthenticationSupport

class EntriesServlet(entryService: EntryService)
  extends ScalatraServlet with JacksonJsonSupport with JValueResult with AuthenticationSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  get("/:id") {
    SafeInt(params("id")) match {
      case Some(id) => entryService.load(id)
      case _ => null
    }
  }

  get("/") {
    entryService.loadAll
  }

  get("/count") {
    JsonWrapper(entryService.count())
  }

  // create new entry
  put("/") {
    haltIfNotAuthenticated()
    val entry: Entry = parsedBody.extract[Entry]

    haltWithForbiddenIf(entry.id >= 0)

    entry.author = user.login
    entryService.add(entry)
  }

  // update existing entry
  post("/") {
    haltIfNotAuthenticated()
    val entry: Entry = parsedBody.extract[Entry]

    haltWithForbiddenIf(entry.id < 0)

    val existingEntry: Entry = entryService.load(entry.id)

    if (existingEntry != null) {
      haltWithForbiddenIf(existingEntry.author.equals(user.login) == false)

      existingEntry.text = entry.text
      entryService.update(existingEntry)
    }
  }


  delete("/:id") {
    haltIfNotAuthenticated()

    SafeInt(params("id")) match {
      case id: Option[Int] =>
        val existingEntry: Entry = entryService.load(id.getOrElse(-1))

        if (existingEntry != null) {
          haltWithForbiddenIf(existingEntry.author.equals(user.login) == false)
          entryService.remove(existingEntry.id)
        }
      case _ => null
    }
  }

}
