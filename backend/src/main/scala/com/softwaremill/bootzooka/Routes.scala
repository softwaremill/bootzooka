package com.softwaremill.bootzooka

import akka.http.scaladsl.server.Directives._
import com.softwaremill.bootzooka.api.{RoutesRequestWrapper, VersionRoutes}
import com.softwaremill.bootzooka.passwordreset.PasswordResetRoutes
import com.softwaremill.bootzooka.user.UsersRoutes

trait Routes extends RoutesRequestWrapper
  with UsersRoutes
  with PasswordResetRoutes
  with VersionRoutes {

  lazy val routes = base {
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
