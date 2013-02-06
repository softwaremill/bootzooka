package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.Entry
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfterAll

class MongoEntryDAOSpec extends FlatSpecWithMongo with ShouldMatchers with BeforeAndAfterAll {
  behavior of "MongoEntryDAO"

  var entryDAO: EntryDAO = null
  val referenceDate = new DateTime().withDate(2012, 12, 10)

  override def beforeAll() {
    super.beforeAll()
    entryDAO = new MongoEntryDAO()
    for (i <- 1 to 3)  {
      entryDAO.add(Entry(_id = new ObjectId(i.toString * 24), text = "Message " + i, authorId = new ObjectId((10-i).toString * 24),
        entered = referenceDate.minusDays(i-1)))
    }
  }

  it should "count all entries" in {
    entryDAO.countItems() should be (3)
  }

  it should "load all entries" in {
    entryDAO.loadAll.size should be (3)
  }

  it should "load single entry" in {
    val entryOpt = entryDAO.load("1" * 24)
    entryOpt should be ('defined)
  }

  it should "not find non existing entry" in {
    val entryOpt = entryDAO.load("19" * 12)
    entryOpt should be (None)
  }

  it should "add new entry" in {
    // Given
    val numberOfEntries = entryDAO.countItems()

    // When
    entryDAO.add(Entry(_id = new ObjectId("a" * 24), text = "Message 9", authorId = new ObjectId("a" * 24),
      entered = new DateTime()))

    // Then
    (entryDAO.countItems() - numberOfEntries) should be (1)
  }

  it should "remove entry" in {
    // Given
    val numberOfEntries = entryDAO.countItems()

    // When
    entryDAO.remove("a" * 24)

    // Then
    (entryDAO.countItems() - numberOfEntries) should be (-1)
  }

  it should "update existing entry" in {
    // Given
    val entryOpt: Option[Entry] = entryDAO.load("1" * 24)
    val message: String = "Updated message"

    // When
    entryOpt.foreach( e => entryDAO.update(e._id.toString, message))

    // Then
    val updatedEntryOpt: Option[Entry] = entryDAO.load("1" * 24)
    updatedEntryOpt match {
      case Some(e) => {
        e.text should be (message)
      }
      case _ => fail("Entry option should be defined")
    }
  }

  it should "load entries sorted from newest" in {
    // When
    val entries = entryDAO.loadAll

    // Then
    entries should have size (3)
    entries(0).entered.isAfter(entries(1).entered) should be (true)
    entries(1).entered.isAfter(entries(2).entered) should be (true)
  }

  it should "find 1 entry newer than given time" in {
    // Given
    val time = referenceDate.minusDays(1).getMillis

    // When
    val counter = entryDAO.countNewerThan(time)

    // Then
    counter should be (1)
  }

  it should "find 3 entries with time 1 ms before the oldest entry time" in {
    // Given
    val time = referenceDate.minusDays(3).minusMillis(1).getMillis

    // When
    val counter = entryDAO.countNewerThan(time)

    // Then
    counter should be (3)
  }

  it should "find no entries with time 1 mssec after the youngest entry time" in {
    // Given
    val time = referenceDate.plusMillis(1).getMillis

    // When
    val counter = entryDAO.countNewerThan(time)

    // Then
    counter should be (0)
  }
}
