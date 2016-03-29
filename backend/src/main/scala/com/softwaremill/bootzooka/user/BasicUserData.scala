package com.softwaremill.bootzooka.user

import java.time.OffsetDateTime
import java.util.UUID

case class BasicUserData(id: UserId, login: String, email: String, createdOn: OffsetDateTime)

object BasicUserData {
  def fromUser(user: User) = new BasicUserData(user.id, user.login, user.email, user.createdOn)
}
