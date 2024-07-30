package com.softwaremill.bootzooka.user

import com.augustnagro.magnum.{Frag, PostgresDbType, Repo, Spec, SqlNameMapper, Table, TableInfo}
import com.password4j.{Argon2Function, Password}
import com.softwaremill.bootzooka.infrastructure.Magnum.{*, given}
import com.softwaremill.bootzooka.user.User.PasswordHashing
import com.softwaremill.bootzooka.user.User.PasswordHashing.Argon2Config.*
import com.softwaremill.bootzooka.util.{Id, LowerCased, PasswordHash, PasswordVerificationStatus, asId, hashedPassword}
import com.softwaremill.tagging.*
import ox.discard

import java.time.Instant

class UserModel:
  private val userRepo = Repo[Users, Users, String]
  private val u = TableInfo[Users, Users, String]

  def insert(user: User)(using DbTx): Unit = userRepo.insert(Users(user))
  def findById(id: Id @@ User)(using DbTx): Option[User] = userRepo.findById(id).map(_.toUser)
  def findByEmail(email: String @@ LowerCased)(using DbTx): Option[User] = findBy(
    Spec[Users].where(sql"${u.emailLowercase} = ${email: String}")
  )
  def findByLogin(login: String @@ LowerCased)(using DbTx): Option[User] = findBy(
    Spec[Users].where(sql"${u.loginLowercase} = ${login: String}")
  )
  def findByLoginOrEmail(loginOrEmail: String @@ LowerCased)(using DbTx): Option[User] =
    findBy(Spec[Users].where(sql"${u.loginLowercase} = ${loginOrEmail: String} OR ${u.emailLowercase} = ${loginOrEmail: String}"))

  private def findBy(by: Spec[Users])(using DbTx): Option[User] =
    userRepo.findAll(by).headOption.map(_.toUser)

  def updatePassword(userId: Id @@ User, newPassword: String @@ PasswordHash)(using DbTx): Unit =
    sql"""UPDATE $u SET ${u.password} = ${newPassword: String} WHERE ${u.id} = ${userId: String}""".update.run().discard

  def updateLogin(userId: Id @@ User, newLogin: String, newLoginLowerCase: String @@ LowerCased)(using DbTx): Unit =
    sql"""UPDATE $u SET ${u.login} = ${newLogin: String}, login_lowercase = ${newLoginLowerCase: String} WHERE ${u.id} = ${userId: String}""".update
      .run()
      .discard

  def updateEmail(userId: Id @@ User, newEmail: String @@ LowerCased)(using DbTx): Unit =
    sql"""UPDATE $u SET ${u.emailLowercase} = ${newEmail: String} WHERE ${u.id} = ${userId: String}""".update.run().discard

end UserModel

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
private case class Users(
    id: String,
    login: String,
    loginLowercase: String,
    emailLowercase: String,
    password: String,
    createdOn: Instant
):
  def toUser: User =
    User(
      id.asId[User],
      login,
      loginLowercase.taggedWith[LowerCased],
      emailLowercase.taggedWith[LowerCased],
      password.taggedWith[PasswordHash],
      createdOn
    )
private object Users:
  def apply(user: User): Users =
    Users(user.id, user.login, user.loginLowerCase, user.emailLowerCase, user.passwordHash, user.createdOn)

case class User(
    id: Id @@ User,
    login: String,
    loginLowerCase: String @@ LowerCased,
    emailLowerCase: String @@ LowerCased,
    passwordHash: String @@ PasswordHash,
    createdOn: Instant
):
  def verifyPassword(password: String): PasswordVerificationStatus =
    if (Password.check(password, passwordHash) `with` PasswordHashing.Argon2) PasswordVerificationStatus.Verified
    else PasswordVerificationStatus.VerificationFailed
end User

object User:
  object PasswordHashing:
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

  def hashPassword(password: String): String @@ PasswordHash =
    Password.hash(password).`with`(PasswordHashing.Argon2).getResult.hashedPassword
end User
