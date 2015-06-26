package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.domain.User
import org.joda.time.{DateTimeZone, DateTime}

trait UserTestHelpers {

  val uuidStr = "de305d54-75b4-431b-adb2-eb6b9e546014"
  val createdOn = new DateTime(2015, 6, 3, 13, 25, 3, DateTimeZone.UTC)

  def newUser(login: String, email: String, pass: String, salt: String, token: String) =
    User.withRandomUUID(login, email, pass, salt, token, createdOn)

}
