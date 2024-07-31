package com.softwaremill.bootzooka.user

import com.augustnagro.magnum.{Frag, PostgresDbType, Repo, Spec, SqlNameMapper, Table, TableInfo}
import com.password4j.{Argon2Function, Password}
import com.softwaremill.bootzooka.infrastructure.Magnum.{*, given}
import com.softwaremill.bootzooka.user.User.PasswordHashing
import com.softwaremill.bootzooka.user.User.PasswordHashing.Argon2Config.*
import com.softwaremill.bootzooka.util.PasswordVerificationStatus
import com.softwaremill.bootzooka.util.Strings.{Hashed, Id, LowerCased, asHashed}
import ox.discard

import java.time.Instant

class UserModel:
  private val userRepo = Repo[Users, Users, Id[User]]
  private val u = TableInfo[Users, Users, Id[User]]

  def insert(user: User)(using DbTx): Unit = userRepo.insert(Users(user))
  def findById(id: Id[User])(using DbTx): Option[User] = userRepo.findById(id).map(_.toUser)
  def findByEmail(email: LowerCased)(using DbTx): Option[User] = findBy(
    Spec[Users].where(sql"${u.emailLowercase} = $email")
  )
  def findByLogin(login: LowerCased)(using DbTx): Option[User] = findBy(
    Spec[Users].where(sql"${u.loginLowercase} = $login")
  )
  def findByLoginOrEmail(loginOrEmail: LowerCased)(using DbTx): Option[User] =
    findBy(Spec[Users].where(sql"${u.loginLowercase} = ${loginOrEmail: String} OR ${u.emailLowercase} = $loginOrEmail"))

  private def findBy(by: Spec[Users])(using DbTx): Option[User] =
    userRepo.findAll(by).headOption.map(_.toUser)

  def updatePassword(userId: Id[User], newPassword: Hashed)(using DbTx): Unit =
    sql"""UPDATE $u SET ${u.password} = $newPassword WHERE ${u.id} = $userId""".update.run().discard

  def updateLogin(userId: Id[User], newLogin: String, newLoginLowerCase: LowerCased)(using DbTx): Unit =
    sql"""UPDATE $u SET ${u.login} = $newLogin, login_lowercase = ${newLoginLowerCase: String} WHERE ${u.id} = $userId""".update
      .run()
      .discard

  def updateEmail(userId: Id[User], newEmail: LowerCased)(using DbTx): Unit =
    sql"""UPDATE $u SET ${u.emailLowercase} = $newEmail WHERE ${u.id} = $userId""".update.run().discard

end UserModel

// TODO: Magnum doesn't support easy customisation of table name
@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
private case class Users(
    id: Id[User],
    login: String,
    loginLowercase: LowerCased,
    emailLowercase: LowerCased,
    password: Hashed,
    createdOn: Instant
):
  def toUser: User = User(id, login, loginLowercase, emailLowercase, password, createdOn)
private object Users:
  def apply(user: User): Users =
    Users(user.id, user.login, user.loginLowerCase, user.emailLowerCase, user.passwordHash, user.createdOn)

case class User(
    id: Id[User],
    login: String,
    loginLowerCase: LowerCased,
    emailLowerCase: LowerCased,
    passwordHash: Hashed,
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

  def hashPassword(password: String): Hashed = Password.hash(password).`with`(PasswordHashing.Argon2).getResult.asHashed
end User
