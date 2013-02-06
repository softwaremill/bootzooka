package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.service.user.UserService
import pl.softwaremill.bootstrap.BootstrapServletSpec
import pl.softwaremill.bootstrap.service.data.{EntriesWithTimeStamp, EntryJson}
import org.json4s.JsonDSL._
import pl.softwaremill.bootstrap.service.entry.EntryService

class EntriesServletSpec extends BootstrapServletSpec {
  behavior of "EntriesServlet"

  def onServletWithMocks(test:(EntryService, UserService) => Unit) {
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

  "GET /" should "return status 200" in {
    onServletWithMocks { (entryService, userService) =>
      get("/") {
        status === 200
        there was one(entryService).loadAll
        there was no(entryService).load("0")
      }
    }
  }

  "GET /" should "return content-type application/json" in {
    onServletWithMocks { (entryService, userService) =>
      get("/") {
        header("Content-Type") contains "application/json"
        there was one(entryService).loadAll
        there was no(entryService).load("0")
      }
    }
  }

  "GET with id" should "return not escaped entry details" in {
    onServletWithMocks{ (entryService, userService) =>
      get("/1", defaultJsonHeaders) {
        body should be (mapToStringifiedJson(Map("id" -> "1", "text"-> "<script>alert('hacker')</script>", "author" -> "Jas Kowalski", "entered" -> "")))
      }
    }
  }

  "GET /" should "return escaped JSON entries" in {
    get("/") {
      body should include ("[{\"id\":\"1\",\"text\":\"&lt;script&gt;alert('hacker')&lt;/script&gt;\",\"author\":\"Jas Kowalski\",\"entered\":\"\"}]")
    }
  }

  "GET /count" should "return number of entries" in {
    onServletWithMocks { (entryService, userService) =>
      get("/count") {
        body should include ("{\"value\":4}")
        there was one(entryService).count()
      }
    }
  }

  "GET /count-newer" should "return number of new entries" in {
    onServletWithMocks { (entryService, userService) =>
      get("count-newer/10000") {
        body should include ("{\"value\":10}")
        there was one(entryService).countNewerThan(10000)
      }
    }
  }

  "POST /" should "return 401 for non logged user" in {
    onServletWithMocks { (entryService, userService) =>
      post("/", "anything") {
        status should be (401)
        there was noCallsTo(entryService)
      }
    }
  }

  "PUT /" should "return 401 for non logged user" in {
    onServletWithMocks { (entryService, userService) =>
      put("/", "anything") {
        status should be (401)
        there was noCallsTo(entryService)
      }
    }
  }
}
