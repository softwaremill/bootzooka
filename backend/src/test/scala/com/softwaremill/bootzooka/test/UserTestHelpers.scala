package com.softwaremill.bootzooka.test

import java.time.{ZoneOffset, OffsetDateTime}

import com.softwaremill.bootzooka.user.User

trait UserTestHelpers {

  val createdOn = OffsetDateTime.of(2015, 6, 3, 13, 25, 3, 0, ZoneOffset.UTC)

  def newUser(login: String, email: String, pass: String, salt: String) =
    User.withRandomUUID(login, email, pass, salt, createdOn)

}
