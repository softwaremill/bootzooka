package com.softwaremill.bootzooka.user

import com.softwaremill.bootzooka.BaseModule
import com.softwaremill.bootzooka.email.{EmailScheduler, EmailTemplatingEngine}
import com.softwaremill.bootzooka.infrastructure.Http
import com.softwaremill.bootzooka.security.{ApiKey, ApiKeyService, Auth}

trait UserModule extends BaseModule {
  lazy val userApi = new UserApi(http, apiKeyAuth, userService)
  lazy val userService = new UserService(emailScheduler, emailTemplatingEngine, apiKeyService, idGenerator, clock, config.user)

  def http: Http
  def apiKeyAuth: Auth[ApiKey]
  def emailScheduler: EmailScheduler
  def emailTemplatingEngine: EmailTemplatingEngine
  def apiKeyService: ApiKeyService
}
