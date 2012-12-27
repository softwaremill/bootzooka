package pl.softwaremill.bootstrap.dao

import com.mongodb.casbah.WriteConcern
import pl.softwaremill.bootstrap.domain.User
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoDB
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import com.mongodb.casbah.query.Imports.ConcreteDBObjectOk

class MongoUserDAO(implicit val mongo: MongoDB) extends SalatDAO[User, ObjectId](mongo("users")) with UserDAO {

  def loadAll = {
    find(MongoDBObject()).toList
  }

  def countItems(): Long = {
    super.count()
  }

  protected def internalAddUser(user: User) {
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

  def findByLowerCasedLogin(login: String) = {
    findOne(MongoDBObject("loginLowerCased" -> login.toLowerCase))
  }

  def findByLoginOrEmail(loginOrEmail: String) = {
    findOne($or(MongoDBObject("loginLowerCased" -> loginOrEmail.toLowerCase), MongoDBObject("email" -> loginOrEmail.toLowerCase)))
  }

  def findByToken(token: String) = {
    findOne(MongoDBObject("token" -> token))
  }

  def findByLoginAndEncryptedPassword(login: String, encryptedPassword: String) = {
    findOne(MongoDBObject("loginLowerCased" -> login.toLowerCase, "password" -> encryptedPassword))
  }

}
