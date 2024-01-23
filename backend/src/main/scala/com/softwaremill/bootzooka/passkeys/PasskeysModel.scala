package com.softwaremill.bootzooka.passkeys

import cats.implicits._
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.user.User
import com.softwaremill.bootzooka.util.Id
import com.softwaremill.tagging.@@

class PasskeysModel {

  def insert(passkey: Passkey): ConnectionIO[Unit] = {
    sql"""INSERT INTO passkeys (user_id, aaguid, coseKey, credentialId, counter)
         |VALUES (${passkey.userId}, ${passkey.aaguid}, ${passkey.coseKey}, ${passkey.credentialId}, ${passkey.counter})
         |ON CONFLICT(passkeys_id) 
         |DO UPDATE SET aaguid = '${passkey.aaguid}', coseKey = '${passkey.coseKey}', credentialId = '${passkey.credentialId}', counter = '${passkey.counter}';
         |""".stripMargin.update.run.void
  }

  def findById(id: Id @@ User): ConnectionIO[Option[Passkey]] = {
    findBy(fr"id = $id")
  }

  private def findBy(by: Fragment): ConnectionIO[Option[Passkey]] = {
    (sql"SELECT user_id, aaguid, coseKey, credentialId, counter FROM passkeys WHERE " ++ by)
      .query[Passkey]
      .option
  }
}

case class Passkey(
    userId: Id @@ User,
    aaguid: String,
    coseKey: String,
    credentialId: Array[Byte],
    counter: Long) {}
