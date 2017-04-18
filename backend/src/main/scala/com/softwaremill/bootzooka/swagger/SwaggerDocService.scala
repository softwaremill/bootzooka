package com.softwaremill.bootzooka.swagger

import com.github.swagger.akka.model.Info

import scala.reflect.runtime.{universe => ua}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.swagger.akka._
import com.softwaremill.bootzooka.version.VersionRoutes
import com.softwaremill.bootzooka.version.BuildInfo._

class SwaggerDocService(address: String, port: Int, system: ActorSystem) extends SwaggerHttpService with HasActorSystem {
  override implicit val actorSystem: ActorSystem = system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override val apiTypes = Seq( // add here routes in order to add to swagger
    ua.typeOf[VersionRoutes]
  )
  override val host = address + ":" + port
  override val info = Info(version = buildDate, title = "Bootzooka")
  override val apiDocsPath = "api-docs"
}
