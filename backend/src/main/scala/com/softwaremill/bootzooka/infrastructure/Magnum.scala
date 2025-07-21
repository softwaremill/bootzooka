package com.softwaremill.bootzooka.infrastructure

import com.augustnagro.magnum.{DbCodec, Frag}
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.util.Strings.*

import java.time.{Instant, OffsetDateTime, ZoneOffset}

/** Import the members of this object when defining SQL queries using Magnum. */
object Magnum extends Logging:
  given DbCodec[Instant] = summon[DbCodec[OffsetDateTime]].biMap(_.toInstant, _.atOffset(ZoneOffset.UTC))

  given idCodec[T]: DbCodec[Id[T]] = DbCodec.StringCodec.biMap(_.asId[T], _.toString)
  given DbCodec[Hashed] = DbCodec.StringCodec.biMap(_.asHashed, _.toString)
  given DbCodec[LowerCased] = DbCodec.StringCodec.biMap(_.toLowerCased, _.toString)

  // proxies to the magnum functions/types, so that we can have only one import
  export com.augustnagro.magnum.{sql, DbTx, DbCon, DbCodec}
end Magnum
