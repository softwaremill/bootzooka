package pl.softwaremill.bootstrap.rest

import org.scalatra.ScalatraServlet
import pl.softwaremill.bootstrap.service.EntryService
import pl.softwaremill.bootstrap.domain.Entry
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s.{DefaultFormats, Formats}
import pl.softwaremill.bootstrap.common.{SafeInt, JsonWrapper}
import pl.softwaremill.bootstrap.auth.AuthenticationSupport

class EntriesServlet extends ScalatraServlet with JacksonJsonSupport with JValueResult with AuthenticationSupport {

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

  // create new entry
  put("/") {
    if (isAuthenticated == false) {
      println("not logged in!!")
      halt(401, "User not logged in")
    }

    val entry: Entry = parsedBody.extract[Entry]

    if (entry.id >= 0) {
      halt(403, "Action forbidden")
    }

    entry.author = user.login
    EntryService.add(entry)
  }

  // update existing entry
  post("/") {
    if (isAuthenticated == false) {
      println("not logged in!!")
      halt(401, "User not logged in")
    }
    val entry: Entry = parsedBody.extract[Entry]

    if (entry.id < 0) {
      halt(403, "Action forbidden")
    }

    val existingEntry: Entry = EntryService.load(entry.id)

    if (existingEntry != null) {
      if (existingEntry.author.equals(user.login)) {
        existingEntry.text = entry.text
        EntryService.update(existingEntry)
      }
      else {
        halt(403, "Action forbidden")
      }
    }
  }

  delete("/:id") {
    if (isAuthenticated == false) {
      halt(401, "User not logged in")
    }
    else {
      SafeInt(params("id")) match {
        case id: Option[Int] =>
          val existingEntry: Entry = EntryService.load(id.getOrElse(-1))

          if (existingEntry != null) {
            if (existingEntry.author.equals(user)) {
              EntryService.remove(existingEntry.id)
            }
            else {
              halt(403, "Action forbidden for this user")
            }
          }
        case _ => null
      }
    }
  }

}
