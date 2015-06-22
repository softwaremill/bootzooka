package com.softwaremill.bootzooka.dao

import java.util.UUID

import com.softwaremill.bootzooka.common.FutureHelpers._
import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import com.softwaremill.bootzooka.domain.{PasswordResetCode, User}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

class PasswordResetCodeDao(protected val database: SqlDatabase)(implicit ec: ExecutionContext)
    extends SqlPasswordResetCodeSchema with SqlUserSchema {

  import database._
  import database.driver.api._

  def add(code: PasswordResetCode): Future[Unit] = {
    db.run(passwordResetCodes += SqlPasswordResetCode(code)).mapToUnit
  }

  def load(code: String): Future[Option[PasswordResetCode]] = findFirstMatching(_.code === code)

  private def findFirstMatching(condition: PasswordResetCodes => Rep[Boolean]): Future[Option[PasswordResetCode]] = {
    val q = for {
      resetCode <- passwordResetCodes.filter(condition)
      user <- resetCode.user
    } yield (resetCode, user)

    val conversion: PartialFunction[(SqlPasswordResetCode, User), PasswordResetCode] = {
      case (rc, u) => PasswordResetCode(rc.id, rc.code, u, rc.validTo)
    }

    db.run(convertFirstResultItem(q.result.headOption, conversion))
  }

  def remove(code: PasswordResetCode): Future[Unit] =
    db.run(passwordResetCodes.filter(_.id === code.id).delete).mapToUnit

  private def convertFirstResultItem[A, B](action: DBIOAction[Option[A], _, _], conversion: (PartialFunction[A, B])) = {
    action.map(_.map(conversion))
  }
}

trait SqlPasswordResetCodeSchema {
  this: SqlUserSchema =>

  protected val database: SqlDatabase

  import database._
  import database.driver.api._

  protected val passwordResetCodes = TableQuery[PasswordResetCodes]

  protected case class SqlPasswordResetCode(id: UUID, code: String, userId: UUID, validTo: DateTime)

  protected object SqlPasswordResetCode extends ((UUID, String, UUID, DateTime) => SqlPasswordResetCode) {
    def apply(rc: PasswordResetCode): SqlPasswordResetCode =
      SqlPasswordResetCode(rc.id, rc.code, rc.user.id, rc.validTo)
  }

  // format: OFF
  protected class PasswordResetCodes(tag: Tag) extends Table[SqlPasswordResetCode](tag, "password_reset_codes") {
    def id        = column[UUID]("id", O.PrimaryKey)
    def code      = column[String]("code")
    def userId    = column[UUID]("user_id")
    def validTo   = column[DateTime]("valid_to")

    def *         = (id, code, userId, validTo) <> (SqlPasswordResetCode.tupled, SqlPasswordResetCode.unapply)

    def user      = foreignKey("password_reset_code_user_fk", userId, users)(
      _.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)
    // format: ON
  }

}