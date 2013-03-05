package pl.softwaremill.bootstrap.domain

import pl.softwaremill.bootstrap.common.Utils
import org.bson.types.ObjectId

case class User(id: ObjectId, login: String, loginLowerCased: String, email: String, password: String, salt: String,
                token: String)

object User {

  def apply(login: String, email: String, plainPassword: String, salt: String, token: String) = {
    new User(null, login, login.toLowerCase, email, encryptPassword(plainPassword, salt), salt, token)
  }

  def encryptPassword(password: String, salt: String) = {
    Utils.sha256(password, salt)
  }

  def passwordsMatch(plainPassword: String, user: User) = {
    user.password.equals(encryptPassword(plainPassword, user.salt))
  }
}
