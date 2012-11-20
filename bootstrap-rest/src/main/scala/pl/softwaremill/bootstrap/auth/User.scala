package pl.softwaremill.bootstrap.auth

import pl.softwaremill.bootstrap.common.Utils

case class User(login: String, password: String) {

  def token: String = Utils.md5(login)

}

object Users {

  def validateToken(token: String) = {
    list.find(_.token == token)
  }

  val list = List(
    User("admin", "admin"),
    User("user", "user")
  )

}
