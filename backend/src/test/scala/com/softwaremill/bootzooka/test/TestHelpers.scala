package com.softwaremill.bootzooka.test

import java.time.{OffsetDateTime, ZoneOffset}

import com.softwaremill.bootzooka.common.crypto.{Argon2dPasswordHashing, CryptoConfig, PasswordHashing}
import com.softwaremill.bootzooka.user.domain.User
import com.typesafe.config.{Config, ConfigFactory}

trait TestHelpers {
  val passwordHashing: PasswordHashing = new Argon2dPasswordHashing(new CryptoConfig {
    override def rootConfig: Config = ConfigFactory.load()
  })

  val createdOn = OffsetDateTime.of(2015, 6, 3, 13, 25, 3, 0, ZoneOffset.UTC)

  private val random     = new scala.util.Random
  private val characters = "abcdefghijklmnopqrstuvwxyz0123456789"

  def randomString(length: Int = 10) =
    Stream.continually(random.nextInt(characters.length)).map(characters).take(length).mkString

  def newUser(login: String, email: String, pass: String, salt: String): User =
    User.withRandomUUID(login, email, pass, salt, createdOn, passwordHashing)

  def newRandomUser(password: Option[String] = None): User = {
    val login = randomString()
    val pass  = password.getOrElse(randomString())
    newUser(login, s"$login@example.com", pass, "someSalt")
  }
}
