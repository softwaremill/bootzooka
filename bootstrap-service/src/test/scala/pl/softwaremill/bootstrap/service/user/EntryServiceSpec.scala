package pl.softwaremill.bootstrap.service.user

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.service.EntryService
import pl.softwaremill.bootstrap.dao.EntryDAO
import org.specs2.specification.Fragment
import org.mockito.Matchers
import org.bson.types.ObjectId
import pl.softwaremill.bootstrap.domain.Entry
import pl.softwaremill.bootstrap.service.data.EntryJson

class EntryServiceSpec  extends Specification with Mockito {

  val validId: String = "1" * 24

  def withCleanMocks(test: (EntryDAO, EntryService) => Fragment ) = {
    val entry = Entry(new ObjectId(validId), "author", "text")
    val entryDAOMock = mock[EntryDAO]
    entryDAOMock.load(new ObjectId(validId)) returns Some(entry)

    val entryService: EntryService = new EntryService(entryDAOMock)

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
        entryService.load(validId)

        there was one(entryDAO).load(Matchers.eq[ObjectId](new ObjectId(validId)))
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
        entryService.remove(validId)

        there was one(entryDAO).remove(Matchers.eq[ObjectId](new ObjectId(validId)))
      }
    })
  }

  "update" should {
    withCleanMocks((entryDAO, entryService) => {
      "ignore invalid objectId" in {
        entryService.update(EntryJson("invalid", "text", "author"))

        there was no(entryDAO).update(any)
      }
    })

    withCleanMocks((entryDAO, entryService) => {
      "call dao.update on correct objectId" in {
        entryService.update(EntryJson(validId, "text", "author"))

        there was one(entryDAO).update((Matchers.eq[Entry](new Entry(new ObjectId(validId), "text", "author"))))
      }
    })
  }
}
