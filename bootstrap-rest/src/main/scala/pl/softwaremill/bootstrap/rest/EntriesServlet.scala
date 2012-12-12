package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.service.user.UserService
import pl.softwaremill.bootstrap.service.EntryService
import pl.softwaremill.bootstrap.common.{NotEscapedJsonWrapper, JsonWrapper}

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
    entryService.add(user.login, entryText)
  }

  // update existing entry
  post("/") {
    haltIfNotAuthenticated()
    val text: String = (parsedBody \ "text").extract[String]
    val id: String = (parsedBody \ "id").extract[String]

    haltWithForbiddenIf(entryService.isAuthor(user.login, id) == false)
    entryService.update(id, text)
  }

  delete("/:id") {
    haltIfNotAuthenticated()

    val id: String = params("id")
    haltWithForbiddenIf(entryService.isAuthor(user.login, id) == false)
    entryService.remove(id)
  }

}
