package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.service.user.UserService
import org.specs2.matcher.MatchResult
import pl.softwaremill.bootstrap.BootstrapServletSpec
import org.json4s.JsonDSL._
import org.json4s.JsonAST.JValue
import pl.softwaremill.bootstrap.service.data.{EntriesWithTimeStamp, UserJson, EntryJson}
import pl.softwaremill.bootstrap.service.entry.EntryService
import pl.softwaremill.bootstrap.domain.User

class EntriesServletWithUserSpec extends BootstrapServletSpec {
  behavior of "EntriesServlet with logged in user"

  val loginJasKowalski = "JasKowalski"

  val entryOne = EntryJson("1", "Message from Jas", loginJasKowalski, "")
  val entryTwo = EntryJson("2", "Message 2 from Jas", loginJasKowalski, "")

  def onServletWithMocks(login: String, testToExecute: (EntryService, UserService) => Unit): Unit = {
    val userService = mock[UserService]

    val entryService = mock[EntryService]
    entryService.count() returns 2
    entryService.loadAll returns EntriesWithTimeStamp(List(entryOne, entryTwo))
    entryService.load("1") returns Some(entryOne)
    entryService.load("2") returns Some(entryTwo)
    entryService.load("3") returns None
    entryService.isAuthor(loginJasKowalski, "1") returns true
    entryService.isAuthor(loginJasKowalski, "2") returns true

    val servlet: EntriesServlet = new EntriesServletWithUser(entryService, userService, login)
    addServlet(servlet, "/*")

    testToExecute(entryService, userService)
  }

  "POST /" should "modify entry that user owns" in {
    onServletWithMocks(login = loginJasKowalski, testToExecute = (entryService, userService) =>
      put("/", mapToJson(Map[String, JValue]("id" -> "1", "text" -> "Important message")), defaultJsonHeaders) {
        there was one(entryService).isAuthor(loginJasKowalski, "1")
        there was one(entryService).update("1", "Important message")
        status should be (200)
      }
    )
  }

  "POST /" should "not update non existing entry" in {
    onServletWithMocks(login = loginJasKowalski, testToExecute = (entryService, userService) =>
      put("/", mapToJson(Map[String, JValue]("id" -> "3", "text" -> "Important message")), defaultJsonHeaders) {
        there was one(entryService).isAuthor(loginJasKowalski, "3")
        there was no(entryService).update(anyString, anyString)
        status should be (403)
      }
    )
  }

  "POST /" should "not update non owner entry" in {
    onServletWithMocks(login = "PiotrNowak", testToExecute = (entryService, usersService) =>
      put("/", mapToJson(Map[String, JValue]("id" -> "2", "text" -> "Important message")), defaultJsonHeaders) {
        there was one(entryService).isAuthor("PiotrNowak", "2")
        there was no(entryService).update(anyString, anyString)
        status should be (403)
      }
    )
  }

  "PUT /" should "create new entry" in {
    onServletWithMocks(login = loginJasKowalski, testToExecute = (entryService, userService) =>
      post("/", mapToJson(Map("text" -> "New message")), defaultJsonHeaders) {
        there was one(entryService).add(loginJasKowalski, "New message")
        there was no(entryService).update(anyString, anyString)
        status should be (200)
      }
    )
  }

  class EntriesServletWithUser(entryService: EntryService, userService: UserService, login: String) extends EntriesServlet(entryService, userService) {
    override def isAuthenticated = true
    override def user = new UserJson(login, "kowalski@kowalski.net", User.encryptPassword("password", "salt"))
  }
}
