package com.softwaremill.bootzooka.rest

import com.softwaremill.bootzooka.service.user.UserService
import com.softwaremill.bootzooka.BootzookaServletSpec
import com.softwaremill.bootzooka.service.data.{EntriesWithTimeStamp, EntryJson}
import org.json4s.JsonDSL._
import com.softwaremill.bootzooka.service.entry.EntryService
import org.mockito.Mockito._

class EntriesServletSpec extends BootzookaServletSpec {
  behavior of "EntriesServlet"

  def onServletWithMocks(test:(EntryService, UserService) => Unit) {
    val userService = mock[UserService]

    val entryService = mock[EntryService]
    when(entryService.count()) thenReturn 4
    val entryOne: EntryJson = EntryJson("1", "<script>alert('hacker')</script>", "Jas Kowalski", "")
    when(entryService.loadAll) thenReturn EntriesWithTimeStamp(List(entryOne))
    when(entryService.load("1")) thenReturn Some(entryOne)
    when(entryService.countNewerThan(10000)) thenReturn 10
    when(entryService.loadAuthoredBy("1")) thenReturn(List(entryOne))


    val servlet: EntriesServlet = new EntriesServlet(entryService, userService, new BootzookaSwagger)
    addServlet(servlet, "/*")

    test(entryService, userService)
  }

  "GET /" should "return status 200" in {
    onServletWithMocks { (entryService, userService) =>
      get("/") {
        status === 200
        verify(entryService).loadAll
        verify(entryService, never()).load("0")
      }
    }
  }

  "GET /" should "return content-type application/json" in {
    onServletWithMocks { (entryService, userService) =>
      get("/") {
        header("Content-Type") contains "application/json"
        verify(entryService).loadAll
        verify(entryService, never()).load("0")
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
        verify(entryService).count()
      }
    }
  }

  "GET /count-newer" should "return number of new entries" in {
    onServletWithMocks { (entryService, userService) =>
      get("count-newer/10000") {
        body should include ("{\"value\":10}")
        verify(entryService).countNewerThan(10000)
      }
    }
  }

  "POST /" should "return 401 for non logged user" in {
    onServletWithMocks { (entryService, userService) =>
      post("/", "anything") {
        status should be (401)
        verifyZeroInteractions(entryService)
      }
    }
  }

  "PUT /" should "return 401 for non logged user" in {
    onServletWithMocks { (entryService, userService) =>
      put("/", "anything") {
        status should be (401)
        verifyZeroInteractions(entryService)
      }
    }
  }

  "GET /author with id" should "return escaped JSON entries" in {
    onServletWithMocks { (entryService, userService) =>
      get("/author/1") {
        body should include ("[{\"id\":\"1\",\"text\":\"&lt;script&gt;alert('hacker')&lt;/script&gt;\",\"author\":\"Jas Kowalski\",\"entered\":\"\"}]")
        verify(entryService, times(1)).loadAuthoredBy("1")
      }
    }
  }
}
