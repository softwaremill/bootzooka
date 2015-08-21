package com.softwaremill.bootzooka.user

import java.util.UUID

import org.joda.time.DateTime

case class UserJson(id: UUID, login: String, email: String, createdOn: DateTime)

object UserJson {
  def apply(user: User) = new UserJson(user.id, user.login, user.email, user.createdOn)
}
