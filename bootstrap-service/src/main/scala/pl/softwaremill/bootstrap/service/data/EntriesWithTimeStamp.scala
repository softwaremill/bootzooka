package pl.softwaremill.bootstrap.service.data

import pl.softwaremill.bootstrap.common.Utils.Clock

case class EntriesWithTimeStamp(entries: List[EntryJson], timestamp: Long = System.currentTimeMillis)

object EntriesWithTimeStamp {

  def apply(entries: List[EntryJson], clock: Clock): EntriesWithTimeStamp = {
    EntriesWithTimeStamp(entries, clock.currentTime)
  }
}

