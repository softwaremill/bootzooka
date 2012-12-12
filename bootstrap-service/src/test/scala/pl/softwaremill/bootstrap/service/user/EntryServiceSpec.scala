package pl.softwaremill.bootstrap.service.user

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.service.EntryService
import pl.softwaremill.bootstrap.dao.{UserDAO, EntryDAO}
import org.specs2.specification.Fragment
import org.bson.types.ObjectId
import pl.softwaremill.bootstrap.domain.Entry

class EntryServiceSpec extends Specification with Mockito {

  val validEntryId: String = "1" * 24
  val validUserId: String = "2" * 24
  val invalidEntryId: String = "-1"
  val validMessage: String = "This is a valid message"

  def withCleanMocks(test: (EntryDAO, EntryService) => Fragment ) = {
    val entry = Entry(new ObjectId(validEntryId), validMessage, new ObjectId(validUserId))

    val entryDAOMock = mock[EntryDAO]
    entryDAOMock.load(validEntryId) returns Some(entry)
    entryDAOMock.load(invalidEntryId) returns None

    val userDAOMock = mock[UserDAO]

    val entryService: EntryService = new EntryService(entryDAOMock, userDAOMock)

    test(entryDAOMock, entryService)
  }

  "load" should {
    withCleanMocks((entryDAO, entryService) => {
      "return EntryJson for valid id" in {
        val entry = entryService.load(validEntryId)

        there was one(entryDAO).load(validEntryId)
        entry.isDefined must beTrue
        entry.get.text must equalTo(validMessage)
      }
    })
    withCleanMocks((entryDAO, entryService) => {
      "return None for invalid id" in {
        val entry = entryService.load(invalidEntryId)

        there was one(entryDAO).load(invalidEntryId)
        entry.isDefined must beFalse
      }
    })
  }

  "remove" should {
    withCleanMocks((entryDAO, entryService) => {
      "delegate to DAO" in {
        entryService.remove(validEntryId)

        there was one(entryDAO).remove(validEntryId)
      }
    })
  }

  "update" should {
    withCleanMocks((entryDAO, entryService) => {
      "delegate to DAO" in {
        entryService.update(validEntryId, "text")

        there was one(entryDAO).update(validEntryId, "text")
      }
    })
  }
}
