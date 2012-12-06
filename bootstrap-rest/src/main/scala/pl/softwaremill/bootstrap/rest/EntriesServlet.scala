package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.service.user.UserService
import pl.softwaremill.bootstrap.service.EntryService
import pl.softwaremill.bootstrap.common.{NotEscapedJsonWrapper, JsonWrapper}
import com.novus.salat.global._
import pl.softwaremill.bootstrap.service.data.EntryJson

class EntriesServlet(entryService: EntryService, val userService: UserService) extends JsonServletWithAuthentication {

  get("/:id") {
    entryService.load(params("id")) match {
      case Some(e) => NotEscapedJsonWrapper(e)
      case _ =>
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
    val entryText =  (parsedBody \ "text").extract[String]

    val entry: EntryJson = new EntryJson("", entryText, user.login)
    entryService.add(entry)
  }

  // update existing entry
  post("/") {
    haltIfNotAuthenticated()
    val text: String = (parsedBody \ "text").extract[String]
    val id: String = (parsedBody \ "id").extract[String]
    val entryJson:EntryJson = EntryJson(id, text, "")

    val existingEntryOpt: Option[EntryJson] = entryService.load(entryJson.id)

    existingEntryOpt.foreach(existingEntry => {
      haltWithForbiddenIf(existingEntry.author != user.login)
      existingEntry.text = entryJson.text
      entryService.update(existingEntry)
    })

  }

  delete("/:id") {
    haltIfNotAuthenticated()

    val existingEntryOpt: Option[EntryJson] = entryService.load(params("id"))
    existingEntryOpt.foreach(entryToRemove => {
      haltWithForbiddenIf(entryToRemove.author != user.login)
      entryService.remove(entryToRemove.id)
    })
  }

}
