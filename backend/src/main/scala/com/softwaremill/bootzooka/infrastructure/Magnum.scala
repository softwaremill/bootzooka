package com.softwaremill.bootzooka.infrastructure

import com.augustnagro.magnum.DbCodec
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.util.Strings.*

import java.time.{Instant, OffsetDateTime, ZoneOffset}

/** Magnum codecs for custom types, useful when writing SQL queries. */
object Magnum extends Logging:
  given DbCodec[Instant] = summon[DbCodec[OffsetDateTime]].biMap(_.toInstant, _.atOffset(ZoneOffset.UTC))

  given idCodec[T]: DbCodec[Id[T]] = DbCodec.StringCodec.biMap(_.asId[T], _.toString)
  given DbCodec[Hashed] = DbCodec.StringCodec.biMap(_.asHashed, _.toString)
  given DbCodec[LowerCased] = DbCodec.StringCodec.biMap(_.toLowerCased, _.toString)
end Magnum
