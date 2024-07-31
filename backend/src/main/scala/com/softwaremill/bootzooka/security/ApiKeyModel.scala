package com.softwaremill.bootzooka.security

import com.augustnagro.magnum.{PostgresDbType, Repo, SqlNameMapper, Table, TableInfo}
import com.softwaremill.bootzooka.infrastructure.Magnum.{*, given}
import com.softwaremill.bootzooka.user.User
import com.softwaremill.bootzooka.util.Strings.Id
import ox.discard

import java.time.Instant

class ApiKeyModel:
  private val apiKeyRepo = Repo[ApiKeys, ApiKeys, Id[ApiKey]]
  private val a = TableInfo[ApiKeys, ApiKeys, Id[ApiKey]]

  def insert(apiKey: ApiKey)(using DbTx): Unit = apiKeyRepo.insert(ApiKeys(apiKey))
  def findById(id: Id[ApiKey])(using DbTx): Option[ApiKey] = apiKeyRepo.findById(id).map(_.toApiKey)
  def deleteAllForUser(id: Id[User])(using DbTx): Unit = sql"""DELETE FROM $a WHERE ${a.userId} = $id""".update.run().discard
  def delete(id: Id[ApiKey])(using DbTx): Unit = apiKeyRepo.deleteById(id)

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class ApiKeys(id: Id[ApiKey], userId: Id[User], createdOn: Instant, validUntil: Instant):
  def toApiKey: ApiKey = ApiKey(id, userId, createdOn, validUntil)

private object ApiKeys:
  def apply(apiKey: ApiKey): ApiKeys = ApiKeys(apiKey.id, apiKey.userId, apiKey.createdOn, apiKey.validUntil)

class ApiKeyAuthToken(apiKeyModel: ApiKeyModel) extends AuthTokenOps[ApiKey]:
  override def tokenName: String = "ApiKey"
  override def findById: DbTx ?=> Id[ApiKey] => Option[ApiKey] = apiKeyModel.findById
  override def delete: DbTx ?=> ApiKey => Unit = ak => apiKeyModel.delete(ak.id)
  override def userId: ApiKey => Id[User] = _.userId
  override def validUntil: ApiKey => Instant = _.validUntil
  override def deleteWhenValid: Boolean = false
