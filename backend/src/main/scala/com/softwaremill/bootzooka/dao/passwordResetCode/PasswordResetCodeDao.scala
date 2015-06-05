package com.softwaremill.bootzooka.dao.passwordResetCode

import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import com.softwaremill.bootzooka.dao.user.SqlUserSchema
import com.softwaremill.bootzooka.domain.{PasswordResetCode, User}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import com.softwaremill.bootzooka.common.FutureHelpers._

class PasswordResetCodeDao(protected val database: SqlDatabase)(implicit ec: ExecutionContext)
  extends SqlPasswordResetCodeSchema with SqlUserSchema {

  import database._
  import database.driver.api._

  def store(code: PasswordResetCode): Future[Unit] = {
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

  def delete(code: PasswordResetCode): Future[Unit] =
    db.run(passwordResetCodes.filter(_.id === code.id).delete).mapToUnit

  private def convertFirstResultItem[A, B](action: DBIOAction[Option[A], _, _], conversion: (PartialFunction[A, B])) = {
    action.map(_.map(conversion))
  }
}
