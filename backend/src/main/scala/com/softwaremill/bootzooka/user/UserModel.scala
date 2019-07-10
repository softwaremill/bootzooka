package com.softwaremill.bootzooka.user

import cats.implicits._
import com.softwaremill.bootzooka.{Id, LowerCased}
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.tagging.@@
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

object UserModel {

  def insert(user: User): ConnectionIO[Unit] = {
    sql"""INSERT INTO users (id, login, login_lowercase, email_lowercase, password, created_on)
         |VALUES (${user.id}, ${user.login}, ${user.loginLowerCased}, ${user.emailLowerCased}, ${user.passwordHash}, ${user.createdOn})""".stripMargin.update.run.void
  }

  def findById(id: Id @@ User): ConnectionIO[Option[User]] = {
    findBy(fr"id = $id")
  }

  def findByEmail(email: String @@ LowerCased): ConnectionIO[Option[User]] = {
    findBy(fr"email_lowercase = $email")
  }

  def findByLogin(login: String @@ LowerCased): ConnectionIO[Option[User]] = {
    findBy(fr"login_lowercase = $login")
  }

  def findByLoginOrEmail(loginOrEmail: String @@ LowerCased): ConnectionIO[Option[User]] = {
    findBy(fr"login_lowercase = $loginOrEmail OR email_lowercase = $loginOrEmail")
  }

  private def findBy(by: Fragment): ConnectionIO[Option[User]] = {
    (sql"SELECT id, login, login_lowercase, email_lowercase, password, created_on FROM users WHERE " ++ by)
      .query[User]
      .option
  }

  def updatePassword(userId: Id @@ User, newPassword: PasswordHash[SCrypt]): ConnectionIO[Unit] =
    sql"""UPDATE users SET password = $newPassword WHERE id = $userId""".stripMargin.update.run.void

  def updateLogin(userId: Id @@ User, newLogin: String, newLoginLowerCase: String @@ LowerCased): ConnectionIO[Unit] =
    sql"""UPDATE users SET login = $newLogin, login_lowercase = $newLoginLowerCase WHERE id = $userId""".stripMargin.update.run.void

  def updateEmail(userId: Id @@ User, newEmail: String @@ LowerCased): ConnectionIO[Unit] =
    sql"""UPDATE users SET email_lowercase = $newEmail WHERE id = $userId""".stripMargin.update.run.void
}
