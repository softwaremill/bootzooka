package com.softwaremill.bootzooka.security

import cats.effect.IO
import com.softwaremill.bootzooka.passwordreset.{PasswordResetAuthToken, PasswordResetCode, PasswordResetCodeModel}
import com.softwaremill.bootzooka.util.BaseModule
import com.softwaremill.macwire._
import doobie.util.transactor.Transactor

trait SecurityModule extends BaseModule {
  lazy val apiKeyModel = new ApiKeyModel
  lazy val apiKeyService = wire[ApiKeyService]
  lazy val apiKeyAuth: Auth[ApiKey] = new Auth(new ApiKeyAuthToken(apiKeyModel), xa, clock)
  lazy val passwordResetCodeAuth: Auth[PasswordResetCode] = new Auth(new PasswordResetAuthToken(passwordResetCodeModel), xa, clock)

  def passwordResetCodeModel: PasswordResetCodeModel
  def xa: Transactor[IO]
}
