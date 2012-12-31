package pl.softwaremill.bootstrap.service.data

import org.joda.time.DateTime

case class EntriesWithTimeStamp(entries: List[EntryJson], timestamp: Long = new DateTime().getMillis)
