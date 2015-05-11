package com.softwaremill.bootzooka.dao.passwordResetCode


import com.softwaremill.bootzooka.domain.PasswordResetCode

import scala.concurrent.Future
import scala.language.implicitConversions

trait PasswordResetCodeDao {

  def store(code: PasswordResetCode): Future[Unit]

  def load(code: String): Future[Option[PasswordResetCode]]

  def delete(code: PasswordResetCode): Future[Unit]
}