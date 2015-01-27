package com.softwaremill.bootzooka.dao.passwordResetCode

import com.softwaremill.bootzooka.domain.PasswordResetCode

import scala.language.implicitConversions

trait PasswordResetCodeDao {

  def store(code: PasswordResetCode): Unit

  def load(code: String): Option[PasswordResetCode]

  def delete(code: PasswordResetCode): Unit
}