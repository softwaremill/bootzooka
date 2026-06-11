package com.softwaremill.bootzooka.user

import ma.chinespirit.parlance.{===, ||, EntityMeta, QueryBuilder, Repo, SqlName, SqlNameMapper, Table, TableInfo, sql}
import com.password4j.{Argon2Function, Password}
import com.softwaremill.bootzooka.infrastructure.Codecs.given
import com.softwaremill.bootzooka.infrastructure.Tx
import com.softwaremill.bootzooka.user.User.PasswordHashing
import com.softwaremill.bootzooka.user.User.PasswordHashing.Argon2Config.*
import com.softwaremill.bootzooka.util.PasswordVerificationStatus
import com.softwaremill.bootzooka.util.Strings.{Hashed, Id, LowerCased, asHashed}
import ox.discard

import java.time.Instant

class UserModel:
  private val userRepo = Repo[User, User, Id[User]]()
  private val u = TableInfo[User, User, Id[User]]

  export userRepo.{rawInsert as insert, findById}

  // queries use parlance's typed column DSL (`_.col === value`), which is checked against the entity's fields at compile time
  def findByEmail(email: LowerCased)(using Tx): Option[User] =
    QueryBuilder.from[User].where(_.emailLowerCase === email).first()
  def findByLogin(login: LowerCased)(using Tx): Option[User] =
    QueryBuilder.from[User].where(_.loginLowerCase === login).first()
  def findByLoginOrEmail(loginOrEmail: LowerCased)(using Tx): Option[User] =
    QueryBuilder.from[User].where(u => u.loginLowerCase === loginOrEmail || u.emailLowerCase === loginOrEmail).first()

  def updatePassword(userId: Id[User], newPassword: Hashed)(using Tx): Unit =
    sql"""UPDATE $u SET ${u.passwordHash} = $newPassword WHERE ${u.id} = $userId""".update.run().discard

  def updateLogin(userId: Id[User], newLogin: String, newLoginLowerCase: LowerCased)(using Tx): Unit =
    sql"""UPDATE $u SET ${u.login} = $newLogin, login_lowercase = ${newLoginLowerCase: String} WHERE ${u.id} = $userId""".update
      .run()
      .discard

  def updateEmail(userId: Id[User], newEmail: LowerCased)(using Tx): Unit =
    sql"""UPDATE $u SET ${u.emailLowerCase} = $newEmail WHERE ${u.id} = $userId""".update.run().discard

end UserModel

@Table(SqlNameMapper.CamelToSnakeCase)
@SqlName("users")
case class User(
    id: Id[User],
    login: String,
    @SqlName("login_lowercase") loginLowerCase: LowerCased,
    @SqlName("email_lowercase") emailLowerCase: LowerCased,
    @SqlName("password") passwordHash: Hashed,
    createdOn: Instant
) derives EntityMeta:
  def verifyPassword(password: String): PasswordVerificationStatus =
    if Password.check(password, passwordHash).`with`(PasswordHashing.Argon2) then PasswordVerificationStatus.Verified
    else PasswordVerificationStatus.VerificationFailed
end User

object User:
  private[user] object PasswordHashing:
    val Argon2: Argon2Function =
      Argon2Function.getInstance(MemoryInKib, NumberOfIterations, LevelOfParallelism, LengthOfTheFinalHash, Type, Version)

    object Argon2Config:
      val MemoryInKib = 12
      val NumberOfIterations = 20
      val LevelOfParallelism = 2
      val LengthOfTheFinalHash = 32
      val Type = com.password4j.types.Argon2.ID
      val Version = 19
  end PasswordHashing

  def hashPassword(password: String): Hashed = Password.hash(password).`with`(PasswordHashing.Argon2).getResult.asHashed
end User
