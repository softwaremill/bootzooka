package pl.softwaremill.bootstrap.dao

import pl.softwaremill.bootstrap.domain.User
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.ObjectIdPk
import com.foursquare.rogue.LiftRogue._
import org.bson.types.ObjectId

class MongoUserDAO extends UserDAO {

  import UserImplicits._

  def loadAll = {
    UserRecord.findAll
  }

  def countItems(): Long = {
    UserRecord.count
  }

  protected def internalAddUser(user: User) {
    user.save
  }

  def remove(userId: String) {
    UserRecord where (_.id eqs new ObjectId(userId)) findAndDeleteOne()
  }

  def load(userId: String): Option[User] = {
    UserRecord where (_.id eqs new ObjectId(userId)) get()
  }

  def findByEmail(email: String) = {
    UserRecord where (_.email eqs email.toLowerCase) get()
  }

  def findByLowerCasedLogin(login: String) = {
    UserRecord where (_.loginLowerCase eqs login.toLowerCase) get()
  }

  def findByLoginOrEmail(loginOrEmail: String) = {
    val lowercased = loginOrEmail.toLowerCase
    UserRecord or(_.where(_.loginLowerCase eqs lowercased), _.where(_.email eqs lowercased)) get()
  }

  def findByToken(token: String) = {
    UserRecord where (_.token eqs token) get()
  }

  def changePassword(userId: String, password: String) {
    UserRecord where (_.id eqs new ObjectId(userId)) modify (_.password setTo password) updateOne()
  }

  def changeLogin(currentLogin: String, newLogin: String) {
    UserRecord where (_.login eqs currentLogin) modify (_.login setTo newLogin) and (_.loginLowerCase setTo newLogin.toLowerCase) updateOne()
  }

  def changeEmail(currentEmail: String, newEmail: String) {
    UserRecord where (_.email eqs currentEmail) modify (_.email setTo newEmail) updateOne()
  }

  private object UserImplicits {
    implicit def fromRecord(user: UserRecord): User = {
      User(user.id.get, user.login.get, user.loginLowerCase.get, user.email.get, user.password.get, user.salt.get, user.token.get)
    }

    implicit def fromRecords(users: List[UserRecord]): List[User] = {
      users.map(fromRecord(_))
    }

    implicit def fromOptionalRecord(userOpt: Option[UserRecord]): Option[User] = {
      userOpt.map(fromRecord(_))
    }

    implicit def toRecord(user: User): UserRecord = {
      UserRecord.createRecord
        .id(user.id)
        .login(user.login)
        .loginLowerCase(user.loginLowerCased)
        .email(user.email)
        .password(user.password)
        .salt(user.salt)
        .token(user.token)
    }
  }

}

private class UserRecord extends MongoRecord[UserRecord] with ObjectIdPk[UserRecord] {
  def meta = UserRecord

  object login extends LongStringField(this)

  object loginLowerCase extends LongStringField(this)

  object email extends LongStringField(this)

  object password extends LongStringField(this)

  object salt extends LongStringField(this)

  object token extends LongStringField(this)

}

private object UserRecord extends UserRecord with MongoMetaRecord[UserRecord] {
  override def collectionName: String = "users"
}