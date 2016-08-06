package com.softwaremill.bootzooka.version

import akka.http.scaladsl.server.Directives._
import com.softwaremill.bootzooka.common.api.RoutesSupport
import com.softwaremill.bootzooka.version.BuildInfo._
import io.circe.generic.auto._

trait VersionRoutes extends RoutesSupport {

  implicit val versionJsonCbs = CanBeSerialized[VersionJson]

  val versionRoutes = path("version") {
    complete {
      VersionJson(buildSha.substring(0, 6), buildDate)
    }
  }
}

case class VersionJson(build: String, date: String)
