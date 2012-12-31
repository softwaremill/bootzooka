package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.service.user.UserService
import org.specs2.matcher.MatchResult
import pl.softwaremill.bootstrap.BootstrapServletSpec
import pl.softwaremill.bootstrap.service.data.{EntriesWithTimeStamp, EntryJson}
import org.json4s.JsonDSL._
import pl.softwaremill.bootstrap.service.entry.EntryService

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class EntriesServletSpec extends BootstrapServletSpec {

  def is =
    sequential ^
      "EntriesServlet" ^
      "GET should return status 200"                        ! root200 ^
      "GET should return content-type application/json"     ! contentJson ^
      "GET with id should return not escaped entry details" ! returnNotEscapedSingleEntryDetails ^
      "GET should return escaped JSON entries"              ! escapedJsonEntries add
      "GET /count on EntriesServlet" ^
        "should return number of entries"                   ! countEntries add
      "GET /count-newer on EntriesServlet" ^
        "should return number of new entries"               ! countNewEntries add
      "POST / on EntriesServiet" ^
        "should return 401 for non logged user"             ! tryUpdateExistingEntry add
      "PUT / on EntriesServiet" ^
        "should return 401 for non logged user"             ! tryCreateNewEntry

  end

  def onServletWithMocks(test:(EntryService, UserService) => MatchResult[Any]): MatchResult[Any] = {
    val userService = mock[UserService]

    val entryService = mock[EntryService]
    entryService.count() returns 4
    val entryOne: EntryJson = EntryJson("1", "<script>alert('hacker')</script>", "Jas Kowalski", "")
    entryService.loadAll returns EntriesWithTimeStamp(List(entryOne))
    entryService.load("1") returns Some(entryOne)
    entryService.countNewerThan(10000) returns 10


    val servlet: EntriesServlet = new EntriesServlet(entryService, userService)
    addServlet(servlet, "/*")

    test(entryService, userService)
  }

  def root200 = onServletWithMocks { (entryService, userService) =>
    get("/") {
      status === 200
      there was one(entryService).loadAll
      there was no(entryService).load("0")
    }
  }

  def contentJson = onServletWithMocks { (entryService, userService) =>
    get("/") {
      header("Content-Type") contains "application/json"
      there was one(entryService).loadAll
      there was no(entryService).load("0")
    }
  }

  def returnNotEscapedSingleEntryDetails = onServletWithMocks{ (entryService, userService) =>
    get("/1", defaultJsonHeaders) {
      body mustEqual(mapToStringifiedJson(Map("id" -> "1", "text"-> "<script>alert('hacker')</script>", "author" -> "Jas Kowalski", "entered" -> "")))
    }
  }

  def escapedJsonEntries = get("/") {
    body must contain("[{\"id\":\"1\",\"text\":\"&lt;script&gt;alert('hacker')&lt;/script&gt;\",\"author\":\"Jas Kowalski\",\"entered\":\"\"}]")
  }

  def countEntries = onServletWithMocks { (entryService, userService) =>
    get("/count") {
      body must contain("{\"value\":4}")
      there was one(entryService).count()
    }
  }

  def countNewEntries = onServletWithMocks { (entryService, userService) =>
    get("count-newer/10000") {
      body must contain("{\"value\":10}")
      there was one(entryService).countNewerThan(10000)
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
