package com.softwaremill.bootzooka.dao.user

import com.softwaremill.bootzooka.dao.sql.SQLDatabase
import com.softwaremill.bootzooka.domain.User

class SQLUserDAO(protected val database: SQLDatabase) extends UserDAO with SQLUserSchema {

  import database._
  import database.driver.simple._

  override def loadAll: List[User] = db.withSession { implicit session =>
    users.list
  }

  override def countItems(): Long = db.withSession { implicit session =>
    users.length.run
  }

  override protected def internalAddUser(user: User): Unit = db.withTransaction { implicit session =>
    users += user
  }

  override def remove(userId: UserId): Unit = db.withTransaction { implicit session =>
    users.filter(_.id === userId).delete
  }

  override def load(userId: UserId): Option[User] =
    findOneWhere(_.id === userId)

  private def findOneWhere(condition: Users => Column[Boolean]): Option[User] = db.withSession { implicit session =>
    users.filter(condition).firstOption
  }

  override def findByEmail(email: String): Option[User] =
    findOneWhere(_.email.toLowerCase === email.toLowerCase)

  override def findByLowerCasedLogin(login: String): Option[User] =
    findOneWhere(_.loginLowerCase === login.toLowerCase)

  override def findByLoginOrEmail(loginOrEmail: String): Option[User] =
    findByLowerCasedLogin(loginOrEmail) orElse findByEmail(loginOrEmail)

  override def findForIdentifiers(uniqueIds: Set[UserId]): List[User] = db.withSession { implicit session =>
    users.filter(_.id inSet uniqueIds).list
  }

  override def findByToken(token: String): Option[User] =
    findOneWhere(_.token === token)

  override def changePassword(userId: UserId, newPassword: String): Unit = db.withTransaction { implicit session =>
    users.filter(_.id === userId).map(_.password).update(newPassword)
  }

  override def changeLogin(currentLogin: String, newLogin: String): Unit = db.withTransaction { implicit session =>
    users.filter(_.loginLowerCase === currentLogin.toLowerCase).map { user =>
      (user.login, user.loginLowerCase)
    }.update((newLogin, newLogin.toLowerCase))
  }

  override def changeEmail(currentEmail: String, newEmail: String): Unit = db.withTransaction { implicit session =>
    users.filter(_.email.toLowerCase === currentEmail.toLowerCase).map(_.email).update(newEmail)
  }
}
