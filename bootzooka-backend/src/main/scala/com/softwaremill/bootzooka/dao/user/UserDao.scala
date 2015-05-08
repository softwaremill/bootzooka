package com.softwaremill.bootzooka.dao.user

import java.util.UUID

import com.softwaremill.bootzooka.domain.User

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure, Try}

trait UserDao {

  type UserId = UUID

  def loadAll(): Future[Seq[User]]
  implicit val ec: ExecutionContext

  def add(user: User): Future[Unit] = {
    val userByLoginFut = findByLowerCasedLogin(user.login)
    val userByEmailFut = findByEmail(user.email)

    for {
      userByLoginOpt <- userByLoginFut
      userByEmailOpt <- userByEmailFut
    } yield {
      if (userByLoginOpt.isDefined || userByEmailOpt.isDefined) {
        throw new IllegalArgumentException("User with given e-mail or login already exists")
      }
      else {
        internalAddUser(user)
      }
    }
  }

  protected def internalAddUser(user: User): Future[Unit]

  def remove(userId: UserId): Future[Unit]

  def load(userId: UserId): Future[Option[User]]

  def findByEmail(email: String): Future[Option[User]]

  def findByLowerCasedLogin(login: String): Future[Option[User]]

  def findByLoginOrEmail(loginOrEmail: String): Future[Option[User]]

  def findForIdentifiers(uniqueIds: Set[UserId]): Future[Seq[User]]

  def findByToken(token: String): Future[Option[User]]

  def changePassword(userId: UserId, password: String): Future[Unit]

  def changeLogin(currentLogin: String, newLogin: String): Future[Unit]

  def changeEmail(currentEmail: String, newEmail: String): Future[Unit]

}
