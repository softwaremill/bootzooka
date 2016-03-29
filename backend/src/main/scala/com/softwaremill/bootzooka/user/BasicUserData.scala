package com.softwaremill.bootzooka.user

import java.time.OffsetDateTime
import java.util.UUID

case class BasicUserData(id: UUID, login: String, email: String, createdOn: OffsetDateTime)

object BasicUserData {
  def apply(user: User) = new BasicUserData(user.id, user.login, user.email, user.createdOn)
}
