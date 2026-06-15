package com.softwaremill.bootzooka.security

import ma.chinespirit.parlance.{EntityMeta, Repo, SqlName, SqlNameMapper, Table, TableInfo, sql}
import com.softwaremill.bootzooka.infrastructure.Codecs.given
import com.softwaremill.bootzooka.infrastructure.Tx
import com.softwaremill.bootzooka.user.User
import com.softwaremill.bootzooka.util.Strings.Id
import ox.discard

import java.time.Instant

class ApiKeyModel:
  private val apiKeyRepo = Repo[ApiKey, ApiKey, Id[ApiKey]]()
  private val a = TableInfo[ApiKey, ApiKey, Id[ApiKey]]

  def insert(apiKey: ApiKey)(using Tx): Unit = apiKeyRepo.rawInsert(apiKey)
  def findById(id: Id[ApiKey])(using Tx): Option[ApiKey] = apiKeyRepo.findById(id)
  def deleteAllForUser(id: Id[User])(using Tx): Unit = sql"""DELETE FROM $a WHERE ${a.userId} = $id""".update.run().discard
  def delete(id: Id[ApiKey])(using Tx): Unit = apiKeyRepo.deleteById(id)
end ApiKeyModel

@Table(SqlNameMapper.CamelToSnakeCase)
@SqlName("api_keys")
case class ApiKey(id: Id[ApiKey], userId: Id[User], createdOn: Instant, validUntil: Instant) derives EntityMeta

class ApiKeyAuthToken(apiKeyModel: ApiKeyModel) extends AuthTokenOps[ApiKey]:
  override def tokenName: String = "ApiKey"
  override def findById: Tx ?=> Id[ApiKey] => Option[ApiKey] = apiKeyModel.findById
  override def delete: Tx ?=> ApiKey => Unit = ak => apiKeyModel.delete(ak.id)
  override def userId: ApiKey => Id[User] = _.userId
  override def validUntil: ApiKey => Instant = _.validUntil
  override def deleteWhenValid: Boolean = false
