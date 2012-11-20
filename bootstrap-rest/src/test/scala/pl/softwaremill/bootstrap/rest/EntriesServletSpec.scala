package pl.softwaremill.bootstrap.rest

import org.scalatra.test.specs2._
import pl.softwaremill.bootstrap.service.EntryService
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.domain.Entry

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class EntriesServletSpec extends ScalatraSpec with Mockito {

  def is =
    "GET / on EntriesServlet" ^
      "should return status 200" ! root200 ^
      "should return content-type application/json" ! contentJson ^
      "should return JSON entries" ! jsonEntries add
      "GET /count on EntriesServlet" ^
        "should return number of entries" ! countEntries

  end

  def mockedService: EntryService = {
    val m = mock[EntryService]
    m.count() returns 4
    m.loadAll returns List(Entry(1, "Jas Kowalski", "Important message"))
    m
  }

  addServlet(new EntriesServlet(mockedService), "/*")

  def root200 = get("/") {
    status must_== 200
  }

  def contentJson = get("/") {
    header.get("Content-Type").get contains "application/json"
  }

  def jsonEntries = get("/") {
    body must contain("[{\"id\":1,\"author\":\"Jas Kowalski\",\"text\":\"Important message\"}]")
  }

  def countEntries = get("/count") {
    body must contain("{\"value\":4}")
  }

}
