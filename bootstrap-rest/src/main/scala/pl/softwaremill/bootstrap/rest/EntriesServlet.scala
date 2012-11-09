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
        haltIfNotAuthorized
        val entry: Entry = parsedBody.extract[Entry]

        haltWithForbiddenIf(entry.id >= 0)

        entry.author = user.login
        EntryService.add(entry)
    }

    // update existing entry
    post("/") {
        haltIfNotAuthorized
        val entry: Entry = parsedBody.extract[Entry]

        haltWithForbiddenIf(entry.id < 0)

        val existingEntry: Entry = EntryService.load(entry.id)

        if (existingEntry != null) {
            haltWithForbiddenIf(existingEntry.author.equals(user.login) == false)

            existingEntry.text = entry.text
            EntryService.update(existingEntry)
        }
    }


    delete("/:id") {
        haltIfNotAuthorized

        SafeInt(params("id")) match {
            case id: Option[Int] =>
                val existingEntry: Entry = EntryService.load(id.getOrElse(-1))

                if (existingEntry != null) {
                    haltWithForbiddenIf(existingEntry.author.equals(user.login) == false)
                    EntryService.remove(existingEntry.id)
                }
            case _ => null
        }
    }

    def haltIfNotAuthorized {
        if (isAuthenticated == false) {
            halt(401, "User not logged in")
        }
    }

    def haltWithForbiddenIf(f: Boolean) {
        if (f) halt(403, "Action forbidden")
    }

}
