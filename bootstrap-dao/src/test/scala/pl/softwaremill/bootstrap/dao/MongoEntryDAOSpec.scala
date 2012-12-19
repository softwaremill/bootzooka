package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.Entry
import org.bson.types.ObjectId
import org.joda.time.DateTime

class MongoEntryDAOSpec extends SpecificationWithMongo {
  var entryDAO: EntryDAO = null

  "MongoEntryDAO" should {

    step({
      println("Creating EntryDAO")
      entryDAO = new MongoEntryDAO()
      for (i <- 1 to 3)  {
        entryDAO.add(Entry(_id = new ObjectId(i.toString * 24), text = "Message " + i, authorId = new ObjectId((10-i).toString * 24),
          entered = new DateTime().minusDays(i)))
      }
    })

    "count all entries" in {
      assert(entryDAO.countItems() === 3)
    }

    "add new entry" in {
      // Given
      val numberOfEntries = entryDAO.countItems()

      // When
      entryDAO.add(Entry(_id = new ObjectId("a" * 24), text = "Message 9", authorId = new ObjectId("a" * 24),
        entered = new DateTime()))

      // Then
      assert(entryDAO.countItems() - numberOfEntries === 1)
    }

    "remove entry" in {
      // Given
      val numberOfEntries = entryDAO.countItems()

      // When
      entryDAO.remove("a" * 24)

      // Then
      assert(entryDAO.countItems() - numberOfEntries === -1)
    }

    "load entries sorted from newest" in {

      // When
      val entries: List[Entry] = entryDAO.loadAll

      // Then
      assert(entries.size === 3)
      assert(entries(0).entered.isAfter(entries(1).entered) === true)
      assert(entries(1).entered.isAfter(entries(2).entered) === true)
    }

  }
}
