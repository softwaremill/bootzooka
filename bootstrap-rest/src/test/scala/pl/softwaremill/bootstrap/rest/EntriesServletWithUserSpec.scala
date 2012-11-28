package pl.softwaremill.bootstrap.rest

import org.scalatra.test.specs2._
import pl.softwaremill.bootstrap.service.user.UserService
import pl.softwaremill.bootstrap.service.EntryService
import org.specs2.mock.Mockito
import org.mockito.Matchers
import pl.softwaremill.bootstrap.domain.{User, Entry}
import org.specs2.matcher.{MatchResult, ThrownExpectations}
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.rest.EntriesServlet


class EntriesServletWithUserSpec extends ScalatraSpec with Mockito with ThrownExpectations {

  val defaultJsonHeaders = Map("Content-Type" -> "application/json;charset=UTF-8")
  val entryOne = Entry(1, "Jas Kowalski", "Message from Jas")
  val entryTwo = Entry(2, "Piotr Nowak", "Message from Piotr")

  def is =
    sequential ^
      "EntriesServlet with logged user" ^
      "POST / request should modify entry that user owns" ! modifyExistingEntryThatLoggedUserOwns ^
      "POST / request should not update non existing entry" ! notUpdateNonExistingEntry ^
      "POST / request should not update non owner entry" ! notAllowToUpdateNotOwnedEntry ^
      "PUT / request should create new entry" ! shouldCreateNewEntry

  end

  def onServletWithMocks(testToExecute: (EntryService, UserService) => MatchResult[Any]): MatchResult[Any] = {
    val userService = mock[UserService]

    val entryService = mock[EntryService]
    entryService.count() returns 2
    entryService.loadAll returns List(entryOne, entryTwo)
    entryService.load(1) returns entryOne
    entryService.load(2) returns entryTwo

    val servlet: EntriesServlet = new EntriesServletWithUser(entryService, userService)
    addServlet(servlet, "/*")

    testToExecute(entryService, userService)
  }

  def modifyExistingEntryThatLoggedUserOwns = onServletWithMocks { (entryService, userService) =>
    post("/", "{\"id\":1,\"text\":\"Important message\"}".getBytes("UTF-8"), defaultJsonHeaders) {
      there was one(entryService).load(1)
      there was one(entryService).update(Matchers.eq(Entry(1, "Jas Kowalski", "Important message")))
      status must_== 200
    }
  }


  def notUpdateNonExistingEntry = onServletWithMocks { (entryService, userService) =>
    post("/", "{\"id\":3,\"text\":\"Important message\"}".getBytes("UTF-8"), defaultJsonHeaders) {
      there was one(entryService).load(3)
      there was no(entryService).update(any[Entry])
      status must_== 200
    }
  }

  def notAllowToUpdateNotOwnedEntry = onServletWithMocks { (entryService, usersService) =>
    post("/", "{\"id\":2,\"text\":\"Important message\"}".getBytes("UTF-8"), defaultJsonHeaders) {
      there was one(entryService).load(2)
      there was no(entryService).update(any[Entry])
      status must_== 403
    }
  }

  def shouldCreateNewEntry = onServletWithMocks { (entryService, userService) =>
    put("/", "{\"text\":\"New message\"}".getBytes("UTF-8"), defaultJsonHeaders) {
      there was one(entryService).add(Matchers.eq(Entry(-1, "Jas Kowalski", "New message")))
      there was no(entryService).update(any[Entry])
      status must_== 200
    }
  }

}

class EntriesServletWithUser(entryService: EntryService, userService: UserService) extends EntriesServlet(entryService, userService) {

  override def isAuthenticated = true

  override def user = new User(1, "Jas Kowalski", "email", "password")
}
