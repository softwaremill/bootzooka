package pl.softwaremill.bootstrap.dao

import com.mongodb.casbah.WriteConcern
import pl.softwaremill.bootstrap.domain.User
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoDB
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.query.Imports.ConcreteDBObjectOk
import com.mongodb.casbah.commons.TypeImports.ObjectId
import com.novus.salat.global._
import java.util.UUID

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

  def changePassword(userId: String, password: String) {
    update(MongoDBObject("_id" -> new ObjectId(userId)), $set("password" -> password), false, false, WriteConcern.Safe)
  }

  def changeLogin(currentLogin: String, newLogin: String) {
    update(MongoDBObject("login" -> currentLogin), $set("login" -> newLogin, "loginLowerCased" -> newLogin.toLowerCase), wc = WriteConcern.Safe)
  }

  def changeEmail(currentEmail: String, newEmail: String) {
    update(MongoDBObject("email" -> currentEmail), $set("email" -> newEmail), wc = WriteConcern.Safe)
  }
}
