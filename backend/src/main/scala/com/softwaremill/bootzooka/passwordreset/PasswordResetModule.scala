package com.softwaremill.bootzooka.passwordreset

import cats.effect.IO
import com.softwaremill.bootzooka.email.{EmailScheduler, EmailTemplates}
import com.softwaremill.bootzooka.http.Http
import com.softwaremill.bootzooka.security.Auth
import com.softwaremill.bootzooka.user.UserModel
import com.softwaremill.bootzooka.util.BaseModule
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.macwire._

trait PasswordResetModule extends BaseModule {
  lazy val passwordResetCodeModel = new PasswordResetCodeModel
  private lazy val passwordResetConfig = config.passwordReset
  lazy val passwordResetService = wire[PasswordResetService]

  lazy val passwordResetApi = wire[PasswordResetApi]

  def userModel: UserModel
  def http: Http
  def passwordResetCodeAuth: Auth[PasswordResetCode]
  def emailScheduler: EmailScheduler
  def emailTemplates: EmailTemplates
  def xa: Transactor[IO]
}
