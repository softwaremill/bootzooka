package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.User
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoDB
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._

class UserDAO(implicit mongoConn: MongoDB) extends SalatDAO[User, ObjectId](mongoConn("users")) {

  def loadAll = {
    find(MongoDBObject()).toList
  }

  def count(): Long = {
    super.count()
  }

  def add(user: User) {
    if (findByLogin(user.login).isDefined || findByEmail(user.email).isDefined) {
      throw new Exception("User with given e-mail or login already exists")
    }

    insert(user, WriteConcern.Safe)
  }

  def remove(userId: String) {
    remove(MongoDBObject("_id" -> new ObjectId(userId)), WriteConcern.Safe)
  }

  def load(userId: String): Option[User] = {
    findOne(MongoDBObject("_id" -> new ObjectId(userId)))
  }

  def findByEmail(email: String) = {
    findOne(MongoDBObject("email" -> email.toLowerCase))
  }

  def findByLogin(login: String) = {
    findOne(MongoDBObject("login" -> login.toLowerCase))
  }

  def findByToken(token: String) = {
    findOne(MongoDBObject("token" -> token))
  }

  def findByLoginAndEncryptedPassword(login: String, encryptedPassword: String) = {
    findOne(MongoDBObject("login" -> login, "password" -> encryptedPassword))
  }
}
