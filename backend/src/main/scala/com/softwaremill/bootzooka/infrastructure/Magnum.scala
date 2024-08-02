package com.softwaremill.bootzooka.infrastructure

import com.augustnagro.magnum.{DbCodec, DbCon, DbTx, Frag}
import com.softwaremill.bootzooka.logging.Logging
import com.softwaremill.bootzooka.util.Strings.*

import java.time.{Instant, OffsetDateTime, ZoneOffset}

/** Import the members of this object when defining SQL queries using Magnum. */
object Magnum extends Logging:
  given DbCodec[Instant] = summon[DbCodec[OffsetDateTime]].biMap(_.toInstant, _.atOffset(ZoneOffset.UTC))

  given idCodec[T]: DbCodec[Id[T]] = DbCodec.StringCodec.biMap(_.asId[T], _.toString)
  given DbCodec[Hashed] = DbCodec.StringCodec.biMap(_.asHashed, _.toString)
  given DbCodec[LowerCased] = DbCodec.StringCodec.biMap(_.toLowerCased, _.toString)

  // TODO use
  // private val SlowThreshold = 200.millis

  // proxies to the magnum functions, so that we can have only one import
  export com.augustnagro.magnum.sql
  type DbTx = com.augustnagro.magnum.DbTx
  type DbCon = com.augustnagro.magnum.DbCon
  type DbCodec[E] = com.augustnagro.magnum.DbCodec[E]

/** Logs the SQL queries which are slow or end up in an exception. */
//  implicit def doobieLogHandler[M[_]: Sync]: LogHandler[M] = new LogHandler[M] {
//    override def run(logEvent: LogEvent): M[Unit] = Sync[M].delay(
//      logEvent match {
//        case Success(sql, _, _, exec, processing) =>
//          if (exec > SlowThreshold || processing > SlowThreshold) {
//            logger.warn(s"Slow query (execution: $exec, processing: $processing): $sql")
//          }
//
//        case ProcessingFailure(sql, args, _, exec, processing, failure) =>
//          logger.error(s"Processing failure (execution: $exec, processing: $processing): $sql | args: $args", failure)
//
//        case ExecFailure(sql, args, _, exec, failure) =>
//          logger.error(s"Execution failure (execution: $exec): $sql | args: $args", failure)
//      }
//    )
//  }
