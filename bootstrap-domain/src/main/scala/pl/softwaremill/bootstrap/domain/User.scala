package pl.softwaremill.bootstrap.domain

import pl.softwaremill.bootstrap.common.Utils
import com.mongodb.casbah.Imports._

case class User(_id: ObjectId = new ObjectId, login: String, email: String, password: String,
                token: String)

object User {

  def apply(login: String, email: String, password: String, token: String) = new User(login = login, email = email,
    password = password, token = token)

  def apply(login: String, email: String, password: String) = new User(login = login, email = email,
    password = password, token = Utils.sha256(password, login))

}
