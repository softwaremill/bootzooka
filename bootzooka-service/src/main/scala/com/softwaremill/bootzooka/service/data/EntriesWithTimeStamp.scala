package com.softwaremill.bootzooka.service.data

import com.softwaremill.common.util.time.Clock

case class EntriesWithTimeStamp(entries: List[EntryJson], timestamp: Long = System.currentTimeMillis)

object EntriesWithTimeStamp {

  def apply(entries: List[EntryJson], clock: Clock): EntriesWithTimeStamp = {
    EntriesWithTimeStamp(entries, clock.currentTimeMillis)
  }
}

