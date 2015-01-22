package com.softwaremill.bootzooka.dao

import com.softwaremill.bootzooka.dao.passwordResetCode.SQLPasswordResetCodeDAO
import com.softwaremill.bootzooka.dao.sql.SQLDatabase
import com.softwaremill.bootzooka.dao.user.SQLUserDAO

trait DAOs {
  lazy val userDao = new SQLUserDAO(sqlDatabase)

  lazy val codeDao = new SQLPasswordResetCodeDAO(sqlDatabase)

  def sqlDatabase: SQLDatabase
}
