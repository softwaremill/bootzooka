package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.common.{ SafeLong, NotEscapedJsonWrapper, JsonWrapper }
import com.softwaremill.bootzooka.service.entry.EntryService
import com.softwaremill.bootzooka.service.data.{EntriesWithTimeStamp, EntryJson}
import org.scalatra.swagger.{SwaggerSupport, Swagger}

class EntriesServlet(entryService: EntryService, val userService: UserService, val swagger: Swagger) extends JsonServletWithAuthentication with EntriesSwaggerDefinition {

  get("/:id", operation(getById)) {
    entryService.load(params("id")) match {
      case Some(e) => NotEscapedJsonWrapper(e)
      case _ =>
    }
  }

  get("/", operation(getAll)) {
    entryService.loadAll
  }

  get("/count", operation(countEntries)) {
    JsonWrapper(entryService.count())
  }

  get("/count-newer/:time", operation(countEntriesSince)) {
    val longOpt = SafeLong(params("time"))

    longOpt match {
      case Some(t) => JsonWrapper(entryService.countNewerThan(t))
      case _ => JsonWrapper(0)
    }
  }

  // create new entry
  post("/", operation(newEntry)) {
    haltIfNotAuthenticated()
    val entryText = (parsedBody \ "text").extract[String]
    entryService.add(user.login, entryText)
  }

  // update existing entry
  put("/", operation(updateEntry)) {
    haltIfNotAuthenticated()
    val text: String = (parsedBody \ "text").extract[String]
    val id: String = (parsedBody \ "id").extract[String]

    haltWithForbiddenIf(entryService.isAuthor(user.login, id) == false)
    entryService.update(id, text)
  }

  delete("/:id", operation(deleteEntry)) {
    haltIfNotAuthenticated()

    val id: String = params("id")
    haltWithForbiddenIf(entryService.isAuthor(user.login, id) == false)
    entryService.remove(id)
  }

}

trait EntriesSwaggerDefinition extends SwaggerSupport {

  override protected val applicationName = Some(EntriesServlet.MAPPING_PATH)
  protected val applicationDescription: String = "Endpoint to fetch entries for timeline"

  val getById = apiOperation[EntryJson]("getEntryById")
    .summary("get entry with given id ")
    .parameter(pathParam[String]("id").description("ID of entry to fetch").optional)

  val getAll = apiOperation[List[EntriesWithTimeStamp]]("getAllEntries")
    .summary("get all entries")

  val countEntries = apiOperation[Long]("countEntries")
    .summary("return number of all entries posted so far")

  val countEntriesSince = apiOperation[Long]("countEntriesSince")
    .summary("return number of entries posted since given time, or 0 if no valid timestamp provided")
    .parameter(pathParam[String]("time").description("timestamp to count entries since").optional)

  val newEntry = apiOperation[Unit]("createNewEntry")
    .notes("Requires user to be authenticated")
    .summary("create fresh entry")
    .parameter(bodyParam[String]("text").description("entry text to send").required)

  val updateEntry = apiOperation[Unit]("updateEntry")
    .notes("Requires user to be authenticated")
    .summary("update existing entry")
    .parameter(bodyParam[String]("text").description("new entry text to set").required)
    .parameter(bodyParam[String]("id").description("entry ID to update text in").required)

  val deleteEntry = apiOperation[EntryJson]("deleteEntry")
    .notes("Requires user to be authenticated")
    .summary("delete entry with given ID")
    .parameter(pathParam[String]("id").description("ID of entry to delete").optional)

}

object EntriesServlet {
  val MAPPING_PATH = "entries"
}
