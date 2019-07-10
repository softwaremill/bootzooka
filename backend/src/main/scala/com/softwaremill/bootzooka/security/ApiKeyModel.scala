package com.softwaremill.bootzooka.security

import java.time.Instant

import cats.implicits._

import com.softwaremill.bootzooka.Id
import com.softwaremill.bootzooka.infrastructure.Doobie
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.user.User
import com.softwaremill.tagging.@@

object ApiKeyModel {

  def insert(apiKey: ApiKey): ConnectionIO[Unit] = {
    sql"""INSERT INTO api_keys (id, user_id, created_on, valid_until)
         |VALUES (${apiKey.id}, ${apiKey.userId}, ${apiKey.createdOn}, ${apiKey.validUntil})""".stripMargin.update.run.void
  }

  def findById(id: Id @@ ApiKey): ConnectionIO[Option[ApiKey]] = {
    sql"""SELECT id, user_id, created_on, valid_until FROM api_keys WHERE id = $id"""
      .query[ApiKey]
      .option
  }

  def delete(id: Id @@ ApiKey): ConnectionIO[Unit] = {
    sql"""DELETE FROM api_keys WHERE id = $id""".update.run.void
  }
}

object ApiKeyAuthToken extends AuthTokenOps[ApiKey] {
  override def tokenName: String = "ApiKey"
  override def findById: Id @@ ApiKey => Doobie.ConnectionIO[Option[ApiKey]] = ApiKeyModel.findById
  override def delete: ApiKey => Doobie.ConnectionIO[Unit] = ak => ApiKeyModel.delete(ak.id)
  override def userId: ApiKey => Id @@ User = _.userId
  override def validUntil: ApiKey => Instant = _.validUntil
  override def deleteWhenValid: Boolean = false
}
