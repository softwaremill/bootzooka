package com.softwaremill.bootzooka.dao.user

import java.util.UUID

import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import com.softwaremill.bootzooka.domain.User
import org.joda.time.DateTime

trait SqlUserSchema {

  protected val database: SqlDatabase

  import database._
  import com.github.tototoshi.slick.H2JodaSupport._
  import database.driver.api._

  protected val users = TableQuery[Users]

  protected class Users(tag: Tag) extends Table[User](tag, "users") {
    def id              = column[UUID]("id", O.PrimaryKey)
    def login           = column[String]("login")
    def loginLowerCase  = column[String]("login_lowercase")
    def email           = column[String]("email")
    def password        = column[String]("password")
    def salt            = column[String]("salt")
    def token           = column[String]("token")
    def createdOn       = column[DateTime]("created_on")

    def * = (id, login, loginLowerCase, email, password, salt, token, createdOn) <>
      (User.tupled, User.unapply)
  }

}
