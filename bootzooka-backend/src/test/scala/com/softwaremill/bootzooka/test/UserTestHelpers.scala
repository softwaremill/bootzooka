package com.softwaremill.bootzooka.test

import com.softwaremill.bootzooka.domain.User
import org.joda.time.DateTime

trait UserTestHelpers {

  val createdOn = new DateTime()

  def newUser(login: String, email: String, pass: String, salt: String, token: String) =
    User(login, email, pass, salt, token, createdOn)

}
