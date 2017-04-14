package com.softwaremill.bootzooka

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import com.softwaremill.bootzooka.common.api.RoutesRequestWrapper
import com.softwaremill.bootzooka.passwordreset.api.PasswordResetRoutes
import com.softwaremill.bootzooka.swagger.SwaggerDocService
import com.softwaremill.bootzooka.user.api.UsersRoutes
import com.softwaremill.bootzooka.version.VersionRoutes
import com.typesafe.config.ConfigFactory

trait Routes extends RoutesRequestWrapper with UsersRoutes with PasswordResetRoutes with VersionRoutes {

  def system: ActorSystem

  lazy val serverConfig = new ServerConfig {
    override def rootConfig = ConfigFactory.load()
  }

  lazy val routes = requestWrapper {
    pathPrefix("api") {
      passwordResetRoutes ~
        usersRoutes ~
        versionRoutes
    } ~
      getFromResourceDirectory("webapp") ~
      new SwaggerDocService(serverConfig.serverHost, serverConfig.serverPort, system).routes ~
      path("") {
        getFromResource("webapp/index.html")
      }
  }
}
