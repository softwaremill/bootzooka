package com.softwaremill.bootzooka.user

import java.time.OffsetDateTime
import java.util.UUID

case class UserJson(id: UUID, login: String, email: String, createdOn: OffsetDateTime)

object UserJson {
  def apply(user: User) = new UserJson(user.id, user.login, user.email, user.createdOn)
}
