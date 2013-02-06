package pl.softwaremill.bootstrap.service.user

import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.dao.{UserDAO, EntryDAO}
import org.bson.types.ObjectId
import pl.softwaremill.bootstrap.domain.Entry
import pl.softwaremill.bootstrap.service.entry.EntryService
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class EntryServiceSpec extends FlatSpec with ShouldMatchers with Mockito {

  val validEntryId: String = "1" * 24
  val validUserId: String = "2" * 24
  val invalidEntryId: String = "-1"
  val validMessage: String = "This is a valid message"

  def withCleanMocks(test: (EntryDAO, EntryService) => Unit) = {
    val entry = Entry(new ObjectId(validEntryId), validMessage, new ObjectId(validUserId))

    val entryDAOMock = mock[EntryDAO]
    entryDAOMock.load(validEntryId) returns Some(entry)
    entryDAOMock.load(invalidEntryId) returns None

    val userDAOMock = mock[UserDAO]

    val entryService: EntryService = new EntryService(entryDAOMock, userDAOMock)

    test(entryDAOMock, entryService)
  }

  "load" should "return EntryJson for valid id" in {
    withCleanMocks((entryDAO, entryService) => {
      val entry = entryService.load(validEntryId)

      there was one(entryDAO).load(validEntryId)
      entry.isDefined should be (true)
      entry.get.text should be (validMessage)
    })
  }

  "load" should "return None for invalid id" in {
    withCleanMocks((entryDAO, entryService) => {
      val entry = entryService.load(invalidEntryId)

      there was one(entryDAO).load(invalidEntryId)
      entry should be (None)
    })
  }

  "remove" should "delegate to DAO" in {
    withCleanMocks((entryDAO, entryService) => {
      entryService.remove(validEntryId)

      there was one(entryDAO).remove(validEntryId)
    })
  }

  "update" should "delegate to DAO" in {
    withCleanMocks((entryDAO, entryService) => {
      entryService.update(validEntryId, "text")

      there was one(entryDAO).update(validEntryId, "text")
    })
  }
}
