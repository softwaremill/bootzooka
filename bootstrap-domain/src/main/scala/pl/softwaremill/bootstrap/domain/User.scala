package pl.softwaremill.bootstrap.domain

import pl.softwaremill.bootstrap.common.Utils
import com.mongodb.casbah.Imports._

case class User(_id: ObjectId = new ObjectId, login: String, loginLowerCased: String, email: String, password: String, salt:String,
  token: String)

object User {

  def apply(login: String, email: String, plainPassword: String, salt:String) = new User(login = login, loginLowerCased = login.toLowerCase,
    email = email, password = encryptPassword(plainPassword, salt), salt = salt, token = generateToken(plainPassword, salt))

  def encryptPassword(password:String, salt:String) = {
    Utils.sha256(password, salt)
  }

  def generateToken(base:String, randomizer:String) = {
    Utils.sha256(base, randomizer)
  }

  def passwordsMatch(plainPassword:String, user:User) = {
    user.password.equals(encryptPassword(plainPassword, user.salt))
  }
}
