package com.softwaremill.bootzooka.dao.passwordResetCode

import java.util.UUID

import com.softwaremill.bootzooka.dao.sql.SQLDatabase
import com.softwaremill.bootzooka.domain.PasswordResetCode
import org.joda.time.DateTime

trait SQLPasswordResetCodeSchema {

  protected val database: SQLDatabase

  import database._
  import database.driver.simple._

  protected val passwordResetCodes = TableQuery[PasswordResetCodes]

  protected class PasswordResetCodes(tag: Tag) extends Table[PasswordResetCode](tag, "passwordResetCodes") {
    def id = column[UUID]("id", O.PrimaryKey)

    def code = column[String]("code")

    def userId = column[UUID]("user_id")

    def validTo = column[DateTime]("valid_to")

    def * = (id, code, userId, validTo) <> (PasswordResetCode.tupled, PasswordResetCode.unapply)
  }

}
