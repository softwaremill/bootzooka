package com.softwaremill.bootzooka.swagger

import com.github.swagger.akka.model.Info

import akka.actor.ActorSystem
import com.github.swagger.akka._
import com.softwaremill.bootzooka.version.VersionRoutes
import com.softwaremill.bootzooka.version.BuildInfo._

class SwaggerDocService(address: String, port: Int, system: ActorSystem) extends SwaggerHttpService {
  override val apiClasses: Set[Class[_]] = Set( // add here routes in order to add to swagger
    classOf[VersionRoutes]
  )
  override val host        = address + ":" + port
  override val info        = Info(version = buildDate, title = "Bootzooka")
  override val apiDocsPath = "api-docs"
}
