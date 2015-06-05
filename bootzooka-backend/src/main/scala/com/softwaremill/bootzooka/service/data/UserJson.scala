package com.softwaremill.bootzooka.service.data

import com.softwaremill.bootzooka.domain.User
import org.joda.time.DateTime

case class UserJson(id: String, login: String, email:String, token: String, createdOn: DateTime)

object UserJson {
  def apply(user: User) = new UserJson(user.id.toString, user.login, user.email, user.token, user.createdOn)

  def apply(list: List[User]): List[UserJson] = {
    for (user <- list) yield UserJson(user)
  }

  def apply(userOpt: Option[User]): Option[UserJson] = userOpt.map(UserJson(_))
}
