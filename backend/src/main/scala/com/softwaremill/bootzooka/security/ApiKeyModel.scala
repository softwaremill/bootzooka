package com.softwaremill.bootzooka.security

import com.augustnagro.magnum.{PostgresDbType, Repo, SqlName, SqlNameMapper, Table, TableInfo}
import com.softwaremill.bootzooka.infrastructure.Magnum.{*, given}
import com.softwaremill.bootzooka.user.User
import com.softwaremill.bootzooka.util.Strings.Id
import ox.discard

import java.time.Instant

class ApiKeyModel:
  private val apiKeyRepo = Repo[ApiKey, ApiKey, Id[ApiKey]]
  private val a = TableInfo[ApiKey, ApiKey, Id[ApiKey]]

  def insert(apiKey: ApiKey)(using DbTx): Unit = apiKeyRepo.insert(apiKey)
  def findById(id: Id[ApiKey])(using DbTx): Option[ApiKey] = apiKeyRepo.findById(id)
  def deleteAllForUser(id: Id[User])(using DbTx): Unit = sql"""DELETE FROM $a WHERE ${a.userId} = $id""".update.run().discard
  def delete(id: Id[ApiKey])(using DbTx): Unit = apiKeyRepo.deleteById(id)

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("api_keys")
case class ApiKey(id: Id[ApiKey], userId: Id[User], createdOn: Instant, validUntil: Instant)

class ApiKeyAuthToken(apiKeyModel: ApiKeyModel) extends AuthTokenOps[ApiKey]:
  override def tokenName: String = "ApiKey"
  override def findById: DbTx ?=> Id[ApiKey] => Option[ApiKey] = apiKeyModel.findById
  override def delete: DbTx ?=> ApiKey => Unit = ak => apiKeyModel.delete(ak.id)
  override def userId: ApiKey => Id[User] = _.userId
  override def validUntil: ApiKey => Instant = _.validUntil
  override def deleteWhenValid: Boolean = false
