package pl.softwaremill.bootstrap.service.user

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.service.EntryService
import pl.softwaremill.bootstrap.dao.{UserDAO, EntryDAO}
import org.specs2.specification.Fragment
import org.mockito.Matchers
import org.bson.types.ObjectId
import pl.softwaremill.bootstrap.domain.Entry
import pl.softwaremill.bootstrap.service.data.EntryJson

class EntryServiceSpec extends Specification with Mockito {

  val validEntryId: String = "1" * 24
  val validUserId: String = "2" * 24

  def withCleanMocks(test: (EntryDAO, EntryService) => Fragment ) = {
    val entry = Entry(new ObjectId(validEntryId), "text", new ObjectId(validUserId))

    val entryDAOMock = mock[EntryDAO]
    entryDAOMock.load(new ObjectId(validEntryId)) returns Some(entry)

    val userDAOMock = mock[UserDAO]

    val entryService: EntryService = new EntryService(entryDAOMock, userDAOMock)

    test(entryDAOMock, entryService)
  }

  "load" should {
    withCleanMocks((entryDAO, entryService) => {
      "ignore invalid objectId" in {
        entryService.load("invalid")

        there was no(entryDAO).load(anyString)
      }
    })

    withCleanMocks((entryDAO, entryService) => {
      "call dao.load on correct objectId" in {
        entryService.load(validEntryId)

        there was one(entryDAO).load(validEntryId)
      }
    })
  }

  "remove" should {
    withCleanMocks((entryDAO, entryService) => {
      "ignore invalid objectId" in {
        entryService.remove("invalid")

        there was no(entryDAO).remove(anyString)
      }
    })

    withCleanMocks((entryDAO, entryService) => {
      "call dao.removeEntry on correct objectId" in {
        entryService.remove(validEntryId)

        there was one(entryDAO).remove(Matchers.eq[ObjectId](new ObjectId(validEntryId)))
      }
    })
  }

  "update" should {
    withCleanMocks((entryDAO, entryService) => {
      "ignore invalid objectId" in {
        entryService.update("invalid", "text")

        there was no(entryDAO).update(anyString, anyString)
      }
    })

    withCleanMocks((entryDAO, entryService) => {
      "call dao.update on correct objectId" in {
        entryService.update(validEntryId, "text")

        there was one(entryDAO).update(validEntryId, "text")
      }
    })
  }
}
