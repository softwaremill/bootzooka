package pl.softwaremill.bootstrap.service.user

import pl.softwaremill.bootstrap.dao.{UserDAO, EntryDAO}
import org.bson.types.ObjectId
import pl.softwaremill.bootstrap.domain.{User, Entry}
import pl.softwaremill.bootstrap.service.entry.EntryService
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import pl.softwaremill.bootstrap.service.data.{EntryJson, EntriesWithTimeStamp}
import pl.softwaremill.bootstrap.common.Utils
import org.joda.time.DateTime
import pl.softwaremill.bootstrap.common.Utils.Clock
import org.scalatest.mock.MockitoSugar
import org.mockito.Matchers._
import org.mockito.Mockito._

class EntryServiceSpec extends FlatSpec with ShouldMatchers with MockitoSugar {

  val validEntryId: String = "1" * 24
  val validUserId: String = "2" * 24
  val invalidEntryId: String = "-1"
  val validEntryPrefix: String = "1" * 23
  val validUserPrefix: String = "2" * 23
  val fixtureTime = 1l
  val fixtureTimeStr = Utils.format(new DateTime(fixtureTime))
  val validMessage: String = "This is a valid message"

  private def entryIdentifier(suffix: Int) = new ObjectId(validEntryPrefix + suffix)
  private def userIdentifier(suffix: Int) = new ObjectId(validUserPrefix + suffix)

  class FixtureTimeClock(val time: Long) extends Clock {
    def currentTime: Long = time
  }

  def withCleanMocks(test: (EntryDAO, EntryService) => Unit) = {
    val entry = Entry(new ObjectId(validEntryId), validMessage, new ObjectId(validUserId), fixtureTime)

    val entryDAOMock = mock[EntryDAO]
    when (entryDAOMock.load(validEntryId)) thenReturn Some(entry)
    when (entryDAOMock.load(invalidEntryId)) thenReturn None

    val userDAOMock = mock[UserDAO]

    val entryService: EntryService = new EntryService(entryDAOMock, userDAOMock)

    test(entryDAOMock, entryService)
  }

  def withMockedCollection(test: (EntryDAO, UserDAO, EntryService) => Unit) = {

    val entries = List(
      Entry(entryIdentifier(0), validMessage, userIdentifier(0), fixtureTime),
      Entry(entryIdentifier(1), validMessage, userIdentifier(1), fixtureTime),
      Entry(entryIdentifier(2), validMessage, userIdentifier(3), fixtureTime),
      Entry(entryIdentifier(3), validMessage, userIdentifier(3), fixtureTime),
      Entry(entryIdentifier(4), validMessage, userIdentifier(2), fixtureTime),
      Entry(entryIdentifier(5), validMessage, userIdentifier(2), fixtureTime))

    val userDAOMock = mock[UserDAO]
    val entryDAOMock = mock[EntryDAO]
    implicit def identifierIntToUser(id: Int): User =
      User(userIdentifier(id), "login" + id, "login" + id, "dummyEmail", "dummyPassword", "dummySalt", "dummyToken");

    when(entryDAOMock.loadAll) thenReturn entries

    val expectedUserList: List[User] = List(0, 1, 3, 2)
    when(userDAOMock.findForIdentifiers(entries.map(_.authorId))) thenReturn(expectedUserList)

    val entryService: EntryService = new EntryService(entryDAOMock, userDAOMock, new FixtureTimeClock(fixtureTime))
    test(entryDAOMock, userDAOMock, entryService)
  }

  "loadAll" should "make exactly two calls to DAOs" in {
    withMockedCollection((entryDAO, userDAO, entryService) => {
      // when
      entryService.loadAll
      // then
      verify(entryDAO).loadAll
      verify(userDAO).findForIdentifiers(any[List[ObjectId]])
    })
  }

  "loadAll" should "return entries with correct user names" in {
    withMockedCollection((entryDAO, userDAO, entryService) => {
      // when
      val allEntries = entryService.loadAll

      // then
      allEntries should be(EntriesWithTimeStamp(List(
        EntryJson(entryIdentifier(0).toString, validMessage, "login0", fixtureTimeStr),
        EntryJson(entryIdentifier(1).toString, validMessage, "login1", fixtureTimeStr),
        EntryJson(entryIdentifier(2).toString, validMessage, "login3", fixtureTimeStr),
        EntryJson(entryIdentifier(3).toString, validMessage, "login3", fixtureTimeStr),
        EntryJson(entryIdentifier(4).toString, validMessage, "login2", fixtureTimeStr),
        EntryJson(entryIdentifier(5).toString, validMessage, "login2", fixtureTimeStr)
      ),new FixtureTimeClock(fixtureTime)))
    })
  }

  "load" should "return EntryJson for valid id" in {
    withCleanMocks((entryDAO, entryService) => {
      val entry = entryService.load(validEntryId)

      verify(entryDAO).load(validEntryId)
      entry.isDefined should be (true)
      entry.get.text should be (validMessage)
    })
  }

  "load" should "return None for invalid id" in {
    withCleanMocks((entryDAO, entryService) => {
      val entry = entryService.load(invalidEntryId)

      verify(entryDAO).load(invalidEntryId)
      entry should be (None)
    })
  }

  "remove" should "delegate to DAO" in {
    withCleanMocks((entryDAO, entryService) => {
      entryService.remove(validEntryId)

      verify(entryDAO).remove(validEntryId)
    })
  }

  "update" should "delegate to DAO" in {
    withCleanMocks((entryDAO, entryService) => {
      entryService.update(validEntryId, "text")

      verify(entryDAO).update(validEntryId, "text")
    })
  }
}
