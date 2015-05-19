package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.dao.passwordResetCode.SqlPasswordResetCodeDao
import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import com.softwaremill.bootzooka.dao.user.SqlUserDao

import scala.concurrent.ExecutionContext

trait Daos {
  implicit val ec: ExecutionContext

  lazy val userDao = new SqlUserDao(sqlDatabase)

  lazy val codeDao = new SqlPasswordResetCodeDao(sqlDatabase)

  def sqlDatabase: SqlDatabase
}
