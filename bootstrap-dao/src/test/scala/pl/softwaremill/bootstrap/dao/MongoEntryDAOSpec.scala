package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.Entry
import org.bson.types.ObjectId
import org.joda.time.DateTime

class MongoEntryDAOSpec extends SpecificationWithMongo {
  var entryDAO: EntryDAO = null

  "MongoEntryDAO" should {

    step({
      entryDAO = new MongoEntryDAO()
      for (i <- 1 to 3)  {
        entryDAO.add(Entry(_id = new ObjectId(i.toString * 24), text = "Message " + i, authorId = new ObjectId((10-i).toString * 24),
          entered = new DateTime().minusDays(i)))
      }
    })

    "count all entries" in {
      assert(entryDAO.countItems() === 3)
    }

    "load all entries" in {
      assert(entryDAO.loadAll.size === 3)
    }

    "load single entry" in {
      val entryOpt: Option[Entry] = entryDAO.load("1" * 24)
      assert(entryOpt.isDefined === true)
    }

    "not find non existing entry" in {
      val entryOpt: Option[Entry] = entryDAO.load("19" * 12)
      assert(entryOpt.isDefined === false)
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

    "update existing entry" in {
      // Given
      val entryOpt: Option[Entry] = entryDAO.load("1" * 24)
      val message: String = "Updated message"

      // When
      entryOpt.foreach( e => entryDAO.update(e._id.toString, message))

      // Then
      val updatedEntryOpt: Option[Entry] = entryDAO.load("1" * 24)
      updatedEntryOpt match {
        case Some(e) => {
          assert(e.text.equals(message) === true)
        }
        case _ => failure("Entry option should be defined")
      }
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
