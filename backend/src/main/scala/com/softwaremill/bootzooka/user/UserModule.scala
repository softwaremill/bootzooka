package com.softwaremill.bootzooka.user

import com.softwaremill.bootzooka.email.{EmailScheduler, EmailTemplates}
import com.softwaremill.bootzooka.http.Http
import com.softwaremill.bootzooka.security.{ApiKey, ApiKeyService, Auth}
import com.softwaremill.bootzooka.util.BaseModule
import doobie.util.transactor.Transactor
import monix.eval.Task

trait UserModule extends BaseModule {
  lazy val userModel = new UserModel
  lazy val userApi = new UserApi(http, apiKeyAuth, userService, xa)
  lazy val userService = new UserService(userModel, emailScheduler, emailTemplates, apiKeyService, idGenerator, clock, config.user)

  def http: Http
  def apiKeyAuth: Auth[ApiKey]
  def emailScheduler: EmailScheduler
  def emailTemplates: EmailTemplates
  def apiKeyService: ApiKeyService
  def xa: Transactor[Task]
}
