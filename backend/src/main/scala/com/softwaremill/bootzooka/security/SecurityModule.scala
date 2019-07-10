package com.softwaremill.bootzooka.security

import com.softwaremill.bootzooka.BaseModule
import com.softwaremill.bootzooka.passwordreset.{PasswordResetAuthToken, PasswordResetCode}
import doobie.util.transactor.Transactor
import monix.eval.Task

trait SecurityModule extends BaseModule {
  lazy val apiKeyService = new ApiKeyService(idGenerator, clock)
  lazy val apiKeyAuth: Auth[ApiKey] = new Auth(ApiKeyAuthToken, xa, clock)
  lazy val passwordResetCodeAuth: Auth[PasswordResetCode] = new Auth(PasswordResetAuthToken, xa, clock)

  def xa: Transactor[Task]
}
