package com.softwaremill.bootzooka.user.domain

import java.time.OffsetDateTime
import java.util.UUID

import com.softwaremill.bootzooka.user._
import de.mkammerer.argon2.Argon2Factory
import de.mkammerer.argon2.Argon2Factory.Argon2Types

case class User(
    id: UserId,
    login: String,
    loginLowerCased: String,
    email: String,
    password: String,
    salt: String,
    createdOn: OffsetDateTime
)

object User {
  val Iterations  = 2
  val Memory      = 16383
  val Parallelism = 4

  def withRandomUUID(
      login: String,
      email: String,
      plainPassword: String,
      salt: String,
      createdOn: OffsetDateTime
  ) = User(UUID.randomUUID(), login, login.toLowerCase, email, encryptPassword(plainPassword, salt), salt, createdOn)

  def encryptPassword(password: String, salt: String): String = {
    val argon2 = Argon2Factory.create(Argon2Types.ARGON2d)
    argon2.hash(Iterations, Memory, Parallelism, salt + password)
  }

  def passwordsMatch(plainPassword: String, user: User): Boolean = {
    val argon2 = Argon2Factory.create(Argon2Types.ARGON2d)
    argon2.verify(user.password, user.salt + plainPassword)
  }
}

case class BasicUserData(id: UserId, login: String, email: String, createdOn: OffsetDateTime)

object BasicUserData {
  def fromUser(user: User) = new BasicUserData(user.id, user.login, user.email, user.createdOn)
}
