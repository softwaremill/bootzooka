package com.softwaremill.bootzooka.dao.passwordResetCode

import java.util.UUID

import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import com.softwaremill.bootzooka.dao.user.SqlUserSchema
import com.softwaremill.bootzooka.domain.PasswordResetCode
import org.joda.time.DateTime

trait SqlPasswordResetCodeSchema {
  this: SqlUserSchema =>

  protected val database: SqlDatabase

  import database._
  import database.driver.simple._

  protected val passwordResetCodes = TableQuery[PasswordResetCodes]

  protected case class SqlPasswordResetCode(id: UUID, code: String, userId: UUID, validTo: DateTime)

  protected object SqlPasswordResetCode extends ((UUID, String, UUID, DateTime) => SqlPasswordResetCode) {
    def apply(rc: PasswordResetCode): SqlPasswordResetCode =
      SqlPasswordResetCode(rc.id, rc.code, rc.user.id, rc.validTo)
  }

  protected class PasswordResetCodes(tag: Tag) extends Table[SqlPasswordResetCode](tag, "password_reset_codes") {
    def id        = column[UUID]("id", O.PrimaryKey)
    def code      = column[String]("code")
    def userId    = column[UUID]("user_id")
    def validTo   = column[DateTime]("valid_to")

    def *         = (id, code, userId, validTo) <> (SqlPasswordResetCode.tupled, SqlPasswordResetCode.unapply)

    def user      = foreignKey("password_reset_code_user_fk", userId, users)(_.id,
                      onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
  }

}
