package com.softwaremill.bootzooka.dao.user

import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import com.softwaremill.bootzooka.domain.User

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.driver.JdbcProfile

class SqlUserDao(protected val database: SqlDatabase)(implicit val ec: ExecutionContext)
  extends UserDao with SqlUserSchema {

  import database._
  import database.driver.api._

  override def loadAll() = db.run(users.result)

  override protected def internalAddUser(user: User): Future[Unit] = {
    db.run(users += user).map(_ => ())
  }

  override def remove(userId: UserId): Future[Unit] = {
    db.run(users.filter(_.id === userId).delete).map(_ => ())
  }

  override def load(userId: UserId): Future[Option[User]] =
    findOneWhere(_.id === userId)

  private def findOneWhere(condition: Users => Rep[Boolean]): Future[Option[User]] = {
    db.run(users.filter(condition).result.headOption)
  }

  override def findByEmail(email: String)  =
    findOneWhere(_.email.toLowerCase === email.toLowerCase)

  override def findByLowerCasedLogin(login: String) =
    findOneWhere(_.loginLowerCase === login.toLowerCase)

  override def findByLoginOrEmail(loginOrEmail: String) = {
    findByLowerCasedLogin(loginOrEmail).flatMap(userOpt =>
      userOpt.map(user => Future{Some(user)}).getOrElse(findByEmail(loginOrEmail))
    )
  }

  override def findForIdentifiers(uniqueIds: Set[UserId]): Future[Seq[User]] = {
      db.run(users.filter(_.id inSet uniqueIds).result)
  }

  override def findByToken(token: String) =
    findOneWhere(_.token === token)

  override def changePassword(userId: UserId, newPassword: String): Future[Unit] = {
    runUnitAction(users.filter(_.id === userId).map(_.password).update(newPassword))
  }

  override def changeLogin(currentLogin: String, newLogin: String): Future[Unit] = {
    val action = users.filter(_.loginLowerCase === currentLogin.toLowerCase).map { user =>
      (user.login, user.loginLowerCase)
    }.update((newLogin, newLogin.toLowerCase))
    runUnitAction(action)
  }

  override def changeEmail(currentEmail: String, newEmail: String): Future[Unit] = {
    val action = users.filter(_.email.toLowerCase === currentEmail.toLowerCase).map(_.email).update(newEmail)
    runUnitAction(action)
  }

  private def runUnitAction[T](action: DBIOAction[_, NoStream, Nothing]) = db.run(action).map(_ => ())
}
