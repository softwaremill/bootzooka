package com.softwaremill.bootzooka.dao.passwordResetCode

import com.softwaremill.bootzooka.domain.PasswordResetCode

class InMemoryPasswordResetCodeDao extends PasswordResetCodeDao {

  private var codes = List[PasswordResetCode]()

  def store(code: PasswordResetCode) {
    codes ::= code
  }

  def load(code: String): Option[PasswordResetCode] = codes.find(_.code == code)

  def delete(code: PasswordResetCode) {
    val index = codes.indexOf(code)
    codes = codes.take(index) ::: codes.drop(index + 1)
  }
}
