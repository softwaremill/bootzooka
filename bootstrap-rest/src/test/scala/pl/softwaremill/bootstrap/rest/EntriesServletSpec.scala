package pl.softwaremill.bootstrap.rest

import org.scalatra.test.specs2._
import pl.softwaremill.bootstrap.service.{UserService, EntryService}
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.domain.Entry
import org.specs2.matcher.MatchResult

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

  def onServletWithMocks(test:(EntryService, UserService) => MatchResult[Any]): MatchResult[Any] = {
    val userService = mock[UserService]

    val entryService = mock[EntryService]
    entryService.count() returns 4
    entryService.loadAll returns List(Entry(1, "Jas Kowalski", "Important message"))

    val servlet: EntriesServlet = new EntriesServlet(entryService, userService)
    addServlet(servlet, "/*")

    test(entryService, userService)
  }

  def root200 = onServletWithMocks { (entryService, userService) =>
    get("/") {
      status === 200
      there was one(userService).loadAll
      there was no(userService).load(0)
    }
  }

  def contentJson = onServletWithMocks { (entryService, userService) =>
    get("/") {
      header("Content-Type") contains "application/json"
      there was one(entryService).loadAll
      there was no(entryService).load(0)
    }
  }

  def jsonEntries = get("/") {
    body must contain("[{\"id\":1,\"author\":\"Jas Kowalski\",\"text\":\"Important message\"}]")
  }

  def countEntries = onServletWithMocks { (entryService, userService) =>
    get("/count") {
      body must contain("{\"value\":4}")
      there was one(entryService).count()
    }
  }

  def tryUpdateExistingEntry = onServletWithMocks { (entryService, userService) =>
    post("/", "anything") {
      status === 401
      there was noCallsTo(entryService)
    }
  }

  def tryCreateNewEntry =  onServletWithMocks { (entryService, userService) =>
    put("/", "anything") {
      status === 401
      there was noCallsTo(entryService)
    }
  }

}
