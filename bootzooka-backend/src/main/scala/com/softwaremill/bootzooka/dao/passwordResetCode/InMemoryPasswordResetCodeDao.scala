package com.softwaremill.bootzooka.dao.passwordResetCode

import com.softwaremill.bootzooka.domain.PasswordResetCode

import scala.concurrent.{ExecutionContext, Future}

class InMemoryPasswordResetCodeDao(implicit ec: ExecutionContext) extends PasswordResetCodeDao {

  private var codes = List[PasswordResetCode]()

  def store(code: PasswordResetCode) {
    codes ::= code
  }

  override def load(code: String) = Future { codes.find(_.code == code) }

  def delete(code: PasswordResetCode) {
    val index = codes.indexOf(code)
    codes = codes.take(index) ::: codes.drop(index + 1)
  }
}
