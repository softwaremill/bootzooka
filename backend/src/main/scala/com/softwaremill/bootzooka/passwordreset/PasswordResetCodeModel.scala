package com.softwaremill.bootzooka.passwordreset

import com.augustnagro.magnum.{PostgresDbType, Repo, SqlNameMapper, Table}

import java.time.Instant
import com.softwaremill.bootzooka.util.{asId, Id}
import com.softwaremill.bootzooka.user.User
import com.softwaremill.tagging.@@
import com.softwaremill.bootzooka.infrastructure.Magnum.{*, given}
import com.softwaremill.bootzooka.security.AuthTokenOps

class PasswordResetCodeModel:
  private val passwordResetCodeRepo = Repo[PasswordResetCodes, PasswordResetCodes, String]
  def insert(pr: PasswordResetCode)(using DbTx): Unit = passwordResetCodeRepo.insert(PasswordResetCodes(pr))
  def delete(id: Id @@ PasswordResetCode)(using DbTx): Unit = passwordResetCodeRepo.deleteById(id)
  def findById(id: Id @@ PasswordResetCode)(using DbTx): Option[PasswordResetCode] =
    passwordResetCodeRepo.findById(id).map(_.toPasswordResetCode)

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
private case class PasswordResetCodes(id: String, userId: String, validUntil: Instant):
  def toPasswordResetCode: PasswordResetCode = PasswordResetCode(id.asId[PasswordResetCode], userId.asId[User], validUntil)

private object PasswordResetCodes:
  def apply(pr: PasswordResetCode): PasswordResetCodes = PasswordResetCodes(pr.id, pr.userId, pr.validUntil)

case class PasswordResetCode(id: Id @@ PasswordResetCode, userId: Id @@ User, validUntil: Instant)

class PasswordResetAuthToken(passwordResetCodeModel: PasswordResetCodeModel) extends AuthTokenOps[PasswordResetCode]:
  override def tokenName: String = "PasswordResetCode"
  override def findById: DbTx ?=> Id @@ PasswordResetCode => Option[PasswordResetCode] = passwordResetCodeModel.findById
  override def delete: DbTx ?=> PasswordResetCode => Unit = ak => passwordResetCodeModel.delete(ak.id)
  override def userId: PasswordResetCode => Id @@ User = _.userId
  override def validUntil: PasswordResetCode => Instant = _.validUntil
  // password reset code is a one-time token
  override def deleteWhenValid: Boolean = true
