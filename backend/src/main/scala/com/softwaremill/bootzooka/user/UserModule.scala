package com.softwaremill.bootzooka.user

import cats.effect.IO
import com.softwaremill.bootzooka.email.{EmailScheduler, EmailTemplates}
import com.softwaremill.bootzooka.http.Http
import com.softwaremill.bootzooka.security.{ApiKey, ApiKeyService, Auth}
import com.softwaremill.bootzooka.util.BaseModule
import com.softwaremill.macwire._
import doobie.util.transactor.Transactor

trait UserModule extends BaseModule {
  lazy val userModel = new UserModel
  private lazy val userConfig = config.user

  lazy val userService = wire[UserService]
  lazy val userApi = wire[UserApi]

  def http: Http
  def apiKeyAuth: Auth[ApiKey]
  def emailScheduler: EmailScheduler
  def emailTemplates: EmailTemplates
  def apiKeyService: ApiKeyService
  def xa: Transactor[IO]
}
