package com.softwaremill.bootzooka.dao.passwordResetCode

import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import com.softwaremill.bootzooka.dao.user.SqlUserSchema
import com.softwaremill.bootzooka.domain.{PasswordResetCode, User}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import com.softwaremill.bootzooka.common.FutureHelpers._

class SqlPasswordResetCodeDao(protected val database: SqlDatabase)(implicit ec: ExecutionContext)
  extends PasswordResetCodeDao with SqlPasswordResetCodeSchema with SqlUserSchema {

  import database._
  import database.driver.api._

  override def store(code: PasswordResetCode): Future[Unit] = {
    db.run(passwordResetCodes += SqlPasswordResetCode(code)).mapToUnit
  }

  override def load(code: String): Future[Option[PasswordResetCode]] = findFirstMatching(_.code === code)

  private def findFirstMatching(condition: PasswordResetCodes => Rep[Boolean]): Future[Option[PasswordResetCode]] = {
    val q = for {
      resetCode <- passwordResetCodes.filter(condition)
      user <- resetCode.user
    } yield (resetCode, user)
    db.run(q.result.headOption.map(_.map {
        case (rc: SqlPasswordResetCode, u: User) => PasswordResetCode(rc.id, rc.code, u, rc.validTo)}))
  }

  override def delete(code: PasswordResetCode): Future[Unit] =
    db.run(passwordResetCodes.filter(_.id === code.id).delete).mapToUnit
}
