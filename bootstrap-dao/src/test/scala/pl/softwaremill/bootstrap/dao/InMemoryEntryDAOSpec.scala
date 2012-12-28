package pl.softwaremill.bootstrap.dao

import org.specs2.mutable.Specification
import org.joda.time.DateTime
import pl.softwaremill.bootstrap.domain.Entry
import org.bson.types.ObjectId

class InMemoryEntryDAOSpec extends Specification {

  "InMemoryEntryDAO" should {

    var entryDAO: EntryDAO = null
    val referenceDate = new DateTime().withDate(2012, 12, 10)

    step({
      entryDAO = new InMemoryEntryDAO()

      for (i <- 1 to 3)  {
        entryDAO.add(Entry(_id = new ObjectId(i.toString * 24), text = "Message " + i, authorId = new ObjectId((10-i).toString * 24),
          entered = referenceDate.minusDays(i-1)))
      }
    })

    "find 1 entry newer than given time" in {
      // Given
      val time = referenceDate.minusDays(1).getMillis

      // When
      val counter = entryDAO.countNewerThan(time)

      // Then
      counter mustEqual 1
    }

    "find 3 entries with time 1 ms before the oldest entry time" in {
      // Given
      val time = referenceDate.minusDays(3).minusMillis(1).getMillis

      // When
      val counter = entryDAO.countNewerThan(time)

      // Then
      counter mustEqual 3
    }

    "find no entries with time 1 mssec after the youngest entry time" in {
      // Given
      val time = referenceDate.plusMillis(1).getMillis

      // When
      val counter = entryDAO.countNewerThan(time)

      // Then
      counter mustEqual 0
    }
  }

}
