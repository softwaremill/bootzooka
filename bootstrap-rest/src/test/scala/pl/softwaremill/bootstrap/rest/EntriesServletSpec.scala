package pl.softwaremill.bootstrap.rest

import org.scalatra.test.specs2._
import pl.softwaremill.bootstrap.service.{UserService, EntryService}
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.domain.Entry

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class EntriesServletSpec extends ScalatraSpec with Mockito {

  def is =
    "EntriesServlet" ^
      "GET should return status 200" ! root200 ^
      "GET should return content-type application/json" ! contentJson ^
      "GET should return JSON entries" ! jsonEntries add
      "GET /count on EntriesServlet" ^
        "should return number of entries" ! countEntries add
      "POST / on EntriesServiet" ^
        "should return 401 for non logged user" ! tryUpdateExistingEntry add
      "PUT / on EntriesServiet" ^
        "should return 401 for non logged user" ! tryCreateNewEntry

  end

  def mockedService: EntryService = {
    val m = mock[EntryService]
    m.count() returns 4
    m.loadAll returns List(Entry(1, "Jas Kowalski", "Important message"))
    m
  }

  def userService = mock[UserService]

  addServlet(new EntriesServlet(mockedService, userService), "/*")

  def root200 = get("/") {
    status must_== 200
  }

  def contentJson = get("/") {
    header("Content-Type") contains "application/json"
  }

  def jsonEntries = get("/") {
    body must contain("[{\"id\":1,\"author\":\"Jas Kowalski\",\"text\":\"Important message\"}]")
  }

  def countEntries = get("/count") {
    body must contain("{\"value\":4}")
  }

  def tryUpdateExistingEntry = post("/", "anything") {
    status must_== 401
  }

  def tryCreateNewEntry = put("/", "anything") {
    status must_== 401
  }


}
