package com.softwaremill.bootzooka.user.application

import java.time.OffsetDateTime
import java.util.UUID

import com.softwaremill.bootzooka.common.FutureHelpers._
import com.softwaremill.bootzooka.common.sql.SqlDatabase
import com.softwaremill.bootzooka.user._
import com.softwaremill.bootzooka.user.domain.{BasicUserData, User}

import scala.concurrent.{ExecutionContext, Future}

class UserDao(protected val database: SqlDatabase)(implicit val ec: ExecutionContext) extends SqlUserSchema {

  import database._
  import database.driver.api._

  def add(user: User): Future[Unit] = db.run(users += user).mapToUnit

  def findById(userId: UserId): Future[Option[User]] = findOneWhere(_.id === userId)

  def findBasicDataById(userId: UserId): Future[Option[BasicUserData]] =
    db.run(users.filter(_.id === userId).map(_.basic).result.headOption)

  private def findOneWhere(condition: Users => Rep[Boolean]) = db.run(users.filter(condition).result.headOption)

  def findByEmail(email: String): Future[Option[User]] = findOneWhere(_.email.toLowerCase === email.toLowerCase)

  def findByLowerCasedLogin(login: String): Future[Option[User]] = findOneWhere(_.loginLowerCase === login.toLowerCase)

  def findByLoginOrEmail(loginOrEmail: String): Future[Option[User]] = {
    findByLowerCasedLogin(loginOrEmail).flatMap(userOpt =>
      userOpt.map(user => Future.successful(Some(user))).getOrElse(findByEmail(loginOrEmail)))
  }

  def changePassword(userId: UserId, newPassword: String): Future[Unit] = {
    db.run(users.filter(_.id === userId).map(_.password).update(newPassword)).mapToUnit
  }

  def changeLogin(userId: UserId, newLogin: String): Future[Unit] = {
    val action = users.filter(_.id === userId).map { user =>
      (user.login, user.loginLowerCase)
    }.update((newLogin, newLogin.toLowerCase))
    db.run(action).mapToUnit
  }

  def changeEmail(userId: UserId, newEmail: String): Future[Unit] = {
    db.run(users.filter(_.id === userId).map(_.email).update(newEmail)).mapToUnit
  }
}

/**
 * The schemas are in separate traits, so that if your DAO would require to access (e.g. join) multiple tables,
 * you can just mix in the necessary traits and have the `TableQuery` definitions available.
 */
trait SqlUserSchema {

  protected val database: SqlDatabase

  import database._
  import database.driver.api._

  protected val users = TableQuery[Users]

  protected class Users(tag: Tag) extends Table[User](tag, "users") {
    // format: OFF
    def id              = column[UUID]("id", O.PrimaryKey)
    def login           = column[String]("login")
    def loginLowerCase  = column[String]("login_lowercase")
    def email           = column[String]("email")
    def password        = column[String]("password")
    def salt            = column[String]("salt")
    def createdOn       = column[OffsetDateTime]("created_on")

    def * = (id, login, loginLowerCase, email, password, salt, createdOn) <> ((User.apply _).tupled, User.unapply)
    def basic = (id, login, email, createdOn) <> ((BasicUserData.apply _).tupled, BasicUserData.unapply)
    // format: ON
  }

}
