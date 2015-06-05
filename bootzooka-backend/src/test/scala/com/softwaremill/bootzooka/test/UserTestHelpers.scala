package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.domain.User
import org.joda.time.{DateTimeZone, DateTime}

trait UserTestHelpers {

  val createdOn = new DateTime(2015, 6, 3, 13, 25, 3, DateTimeZone.UTC)

  def newUser(login: String, email: String, pass: String, salt: String, token: String) =
    User(login, email, pass, salt, token, createdOn)

}
