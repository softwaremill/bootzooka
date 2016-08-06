package com.softwaremill.bootzooka

import akka.http.scaladsl.server.Directives._
import com.softwaremill.bootzooka.common.api.RoutesRequestWrapper
import com.softwaremill.bootzooka.passwordreset.api.PasswordResetRoutes
import com.softwaremill.bootzooka.user.api.UsersRoutes
import com.softwaremill.bootzooka.version.VersionRoutes

trait Routes extends RoutesRequestWrapper
    with UsersRoutes
    with PasswordResetRoutes
    with VersionRoutes {

  lazy val routes = requestWrapper {
    pathPrefix("api") {
      passwordResetRoutes ~
        usersRoutes ~
        versionRoutes
    } ~
      getFromResourceDirectory("webapp") ~
      path("") {
        getFromResource("webapp/index.html")
      }
  }
}
