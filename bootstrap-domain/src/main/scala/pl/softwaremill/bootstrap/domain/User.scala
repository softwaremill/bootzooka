package pl.softwaremill.bootstrap.domain

import pl.softwaremill.bootstrap.common.Utils

case class User(id: Int, login: String, email: String, password: String) {

  def this(login: String, email: String, password: String) = this(-1, login, email, password)

  def token: String = Utils.sha256(login, password)

  def valid() = {
    login.length > 2 && email.length > 0 && password.length > 0
  }

  override def toString = {
    "[User: id = " + id + " login = " + login+ ", email = " + email + ", password = " + password + "]"
  }
}
