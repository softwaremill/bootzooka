package com.softwaremill.bootzooka.dao.passwordResetCode

import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import com.softwaremill.bootzooka.dao.user.SqlUserSchema
import com.softwaremill.bootzooka.domain.{PasswordResetCode, User}

import scala.language.implicitConversions

class SqlPasswordResetCodeDao(protected val database: SqlDatabase) extends PasswordResetCodeDao with SqlPasswordResetCodeSchema with SqlUserSchema {

  import database._
  import database.driver.simple._

  override def store(code: PasswordResetCode): Unit = db.withSession { implicit session =>
    passwordResetCodes.insert(SqlPasswordResetCode(code))
  }

  override def load(code: String): Option[PasswordResetCode] = findFirstMatching(_.code === code)

  private def findFirstMatching(condition: PasswordResetCodes => Column[Boolean]): Option[PasswordResetCode] =
    db.withTransaction { implicit session =>
      val q = for {
        resetCode <- passwordResetCodes.filter(condition)
        user <- resetCode.user
      } yield (resetCode, user)
      q.firstOption.map {
        case (rc: SqlPasswordResetCode, u: User) => PasswordResetCode(rc.id, rc.code, u, rc.validTo)
      }
    }

  override def delete(code: PasswordResetCode): Unit = db.withTransaction { implicit session =>
    passwordResetCodes.filter(_.id === code.id).delete
  }
}
