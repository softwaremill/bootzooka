package com.softwaremill.bootzooka.user

import com.augustnagro.magnum.{Frag, PostgresDbType, Repo, Spec, SqlName, SqlNameMapper, Table, TableInfo}
import com.password4j.{Argon2Function, Password}
import com.softwaremill.bootzooka.infrastructure.Magnum.{*, given}
import com.softwaremill.bootzooka.user.User.PasswordHashing
import com.softwaremill.bootzooka.user.User.PasswordHashing.Argon2Config.*
import com.softwaremill.bootzooka.util.PasswordVerificationStatus
import com.softwaremill.bootzooka.util.Strings.{Hashed, Id, LowerCased, asHashed}
import ox.discard

import java.time.Instant

class UserModel:
  private val userRepo = Repo[User, User, Id[User]]
  private val u = TableInfo[User, User, Id[User]]

  def insert(user: User)(using DbTx): Unit = userRepo.insert(user)
  def findById(id: Id[User])(using DbTx): Option[User] = userRepo.findById(id)
  def findByEmail(email: LowerCased)(using DbTx): Option[User] = findBy(
    Spec[User].where(sql"${u.emailLowerCase} = $email")
  )
  def findByLogin(login: LowerCased)(using DbTx): Option[User] = findBy(
    Spec[User].where(sql"${u.loginLowerCase} = $login")
  )
  def findByLoginOrEmail(loginOrEmail: LowerCased)(using DbTx): Option[User] =
    findBy(Spec[User].where(sql"${u.loginLowerCase} = ${loginOrEmail: String} OR ${u.emailLowerCase} = $loginOrEmail"))

  private def findBy(by: Spec[User])(using DbTx): Option[User] =
    userRepo.findAll(by).headOption

  def updatePassword(userId: Id[User], newPassword: Hashed)(using DbTx): Unit =
    sql"""UPDATE $u SET ${u.passwordHash} = $newPassword WHERE ${u.id} = $userId""".update.run().discard

  def updateLogin(userId: Id[User], newLogin: String, newLoginLowerCase: LowerCased)(using DbTx): Unit =
    sql"""UPDATE $u SET ${u.login} = $newLogin, login_lowercase = ${newLoginLowerCase: String} WHERE ${u.id} = $userId""".update
      .run()
      .discard

  def updateEmail(userId: Id[User], newEmail: LowerCased)(using DbTx): Unit =
    sql"""UPDATE $u SET ${u.emailLowerCase} = $newEmail WHERE ${u.id} = $userId""".update.run().discard

end UserModel

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("users")
case class User(
    id: Id[User],
    login: String,
    @SqlName("login_lowercase") loginLowerCase: LowerCased,
    @SqlName("email_lowercase") emailLowerCase: LowerCased,
    @SqlName("password") passwordHash: Hashed,
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
