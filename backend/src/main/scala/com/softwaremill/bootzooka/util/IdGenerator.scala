package com.softwaremill.bootzooka.util

import cats.effect.Sync
import com.softwaremill.tagging._
import tsec.common.SecureRandomId

/** Any effects that are run as part of transactions and outside of transactions, need to be parametrised with the effect type. */
trait IdGenerator {
  def nextId[F[_]: Sync, U](): F[Id @@ U]
}

object DefaultIdGenerator extends IdGenerator {
  override def nextId[F[_]: Sync, U](): F[Id @@ U] = Sync[F].delay { SecureRandomId.Strong.generate.taggedWith[U] }
}
