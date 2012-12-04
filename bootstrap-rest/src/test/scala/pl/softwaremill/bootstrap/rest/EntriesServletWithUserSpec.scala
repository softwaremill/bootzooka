package pl.softwaremill.bootstrap.rest

import pl.softwaremill.bootstrap.service.user.UserService
import pl.softwaremill.bootstrap.service.EntryService
import org.mockito.Matchers
import org.specs2.matcher.MatchResult
import pl.softwaremill.bootstrap.BootstrapServletSpec
import pl.softwaremill.bootstrap.common.Utils
import org.json4s.JsonDSL._
import org.json4s.JsonAST.JValue
import pl.softwaremill.bootstrap.service.data.{UserJson, EntryJson}


class EntriesServletWithUserSpec extends BootstrapServletSpec {

    def is =
      sequential ^
        "EntriesServlet with logged user" ^
        "POST / request should modify entry that user owns" ! modifyExistingEntryThatLoggedUserOwns ^
        "POST / request should not update non existing entry" ! notUpdateNonExistingEntry ^
        "POST / request should not update non owner entry" ! notAllowToUpdateNotOwnedEntry ^
        "PUT / request should create new entry" ! shouldCreateNewEntry ^
        "POST / should escape text message" ! postShouldCallServiceUsingEscapedStrings
        "PUT / should escape text message" ! putShouldCallServiceUsingEscapedStrings
    end

  val entryOne = EntryJson("1", "Message from Jas", "Jas Kowalski")
  val entryTwo = EntryJson("2", "Message from Piotr", "Piotr Nowak")

  def onServletWithMocks(testToExecute: (EntryService, UserService) => MatchResult[Any]): MatchResult[Any] = {
    val userService = mock[UserService]

    val entryService = mock[EntryService]
    entryService.count() returns 2
    entryService.loadAll returns List(entryOne, entryTwo)
    entryService.load("1") returns Some(entryOne)
    entryService.load("2") returns Some(entryTwo)
    entryService.load("3") returns None

    val servlet: EntriesServlet = new EntriesServletWithUser(entryService, userService)
    addServlet(servlet, "/*")

    testToExecute(entryService, userService)
  }

  def modifyExistingEntryThatLoggedUserOwns = onServletWithMocks { (entryService, userService) =>
    post("/", mapToJson(Map[String, JValue]("id" -> "1", "text" -> "Important message")), defaultJsonHeaders) {
      there was one(entryService).load("1")
      there was one(entryService).update(Matchers.eq(EntryJson("1", "Important message", "Jas Kowalski")))
      status must_== 200
    }
  }

  def notUpdateNonExistingEntry = onServletWithMocks { (entryService, userService) =>
    post("/", mapToJson(Map[String, JValue]("id" -> "3", "text"-> "Important message")), defaultJsonHeaders) {
      there was one(entryService).load("3")
      there was no(entryService).update(any[EntryJson])
      status must_== 200
    }
  }

  def notAllowToUpdateNotOwnedEntry = onServletWithMocks { (entryService, usersService) =>
    post("/", mapToJson(Map[String, JValue]("id" -> "2", "text"-> "Important message")), defaultJsonHeaders) {
      there was one(entryService).load("2")
      there was no(entryService).update(any[EntryJson])
      status must_== 403
    }
  }

  def shouldCreateNewEntry = onServletWithMocks { (entryService, userService) =>
    put("/", mapToJson(Map("text"-> "New message")), defaultJsonHeaders) {
      there was one(entryService).add(Matchers.eq(EntryJson("", "New message", "Jas Kowalski" )))
      there was no(entryService).update(any[EntryJson])
      status must_== 200
    }
  }

  def postShouldCallServiceUsingEscapedStrings = onServletWithMocks{(entryService, userService) =>
    post("/", mapToJson(Map[String, JValue]("id" -> "1", "text"-> "<script>alert('haxor');</script>")), defaultJsonHeaders) {
      there was one(entryService).update(Matchers.eq(EntryJson("1", "&lt;script&gt;alert('haxor');&lt;/script&gt;", "Jas Kowalski")))
    }
  }

  def putShouldCallServiceUsingEscapedStrings = onServletWithMocks{(entryService, userService) =>
    put("/", mapToJson(Map[String, JValue]("text"-> "<script>alert('haxor');</script>")), defaultJsonHeaders) {
      there was one(entryService).add(Matchers.eq(EntryJson("", "&lt;script&gt;alert('haxor');&lt;/script&gt;", "Jas Kowalski")))
    }
  }


}

class EntriesServletWithUser(entryService: EntryService, userService: UserService) extends EntriesServlet(entryService, userService) {

  override def isAuthenticated = true

  override def user = new UserJson("Jas Kowalski", Utils.sha256("password", "Jas Kowalski"))
}
