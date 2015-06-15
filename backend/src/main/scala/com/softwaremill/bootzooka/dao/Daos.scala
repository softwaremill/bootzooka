package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.dao.sql.SqlDatabase

import scala.concurrent.ExecutionContext

trait Daos {
  implicit val ec: ExecutionContext

  lazy val userDao = new UserDao(sqlDatabase)

  lazy val codeDao = new PasswordResetCodeDao(sqlDatabase)

  def sqlDatabase: SqlDatabase
}
