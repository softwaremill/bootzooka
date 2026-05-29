package com.softwaremill.bootzooka.infrastructure

import ma.chinespirit.parlance.DbCodec
import com.softwaremill.bootzooka.util.Strings.*

import java.time.{Instant, OffsetDateTime, ZoneOffset}

/** parlance [[DbCodec]]s for custom types, useful when writing SQL queries. */
object Codecs:
  given DbCodec[Instant] = summon[DbCodec[OffsetDateTime]].biMap(_.toInstant, _.atOffset(ZoneOffset.UTC))

  given idCodec[T]: DbCodec[Id[T]] = DbCodec.StringCodec.biMap(_.asId[T], _.toString)
  given DbCodec[Hashed] = DbCodec.StringCodec.biMap(_.asHashed, _.toString)
  given DbCodec[LowerCased] = DbCodec.StringCodec.biMap(_.toLowerCased, _.toString)
end Codecs
