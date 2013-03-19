package com.softwaremill.bootzooka.service.data

import com.softwaremill.bootzooka.domain.User

case class UserJson(login: String, email:String, token: String)

object UserJson {
  def apply(user: User) = new UserJson(user.login, user.email, user.token)

  def apply(list: List[User]): List[UserJson] = {
    for (user <- list) yield UserJson(user)
  }

  def apply(userOpt: Option[User]): Option[UserJson] = {
    userOpt match {
      case Some(user) => new Some(UserJson(user))
      case _ => None
    }
  }
}
