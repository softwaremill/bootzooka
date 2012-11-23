package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.service.{UserService, EntryService}
import pl.softwaremill.bootstrap.domain.Entry
import pl.softwaremill.bootstrap.common.{SafeInt, JsonWrapper}
import pl.softwaremill.bootstrap.auth.AuthenticationSupport

class EntriesServlet(entryService: EntryService, val userService: UserService) extends JsonServletWithAuthentication {

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
    val entry = parsedBody.extract[Entry]

    haltWithForbiddenIf(entry.id >= 0)

    entry.author = user.login
    entryService.add(entry)
  }

  // update existing entry
  post("/") {
    haltIfNotAuthenticated()
    val entry = parsedBody.extract[Entry]

    haltWithForbiddenIf(entry.id < 0)

    val existingEntry = entryService.load(entry.id)
    if (existingEntry != null) {
      haltWithForbiddenIf(existingEntry.author != user.login)

      existingEntry.text = entry.text
      entryService.update(existingEntry)
    }
  }

  delete("/:id") {
    haltIfNotAuthenticated()

    SafeInt(params("id")) match {
      case Some(id) =>
        val existingEntry = entryService.load(id)
        if (existingEntry != null) {
          haltWithForbiddenIf(existingEntry.author != user.login)
          entryService.remove(existingEntry.id)
        }
      case _ => null
    }
  }

}
