package com.softwaremill.bootzooka.rest.swagger

import com.softwaremill.bootzooka.rest.Mappable
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.scalatra.ScalatraServlet
import org.scalatra.swagger.{ApiInfo, NativeSwaggerBase, Swagger}

class SwaggerServlet(implicit val swagger: Swagger) extends ScalatraServlet with NativeSwaggerBase with LazyLogging with Mappable {

  override def mappingPath: String = "api-docs"
}

object BootzookaSwagger {

  val Info = ApiInfo(
    "Bootzooka API",
    "Docs for the Bootzooka API",
    "http://bootzooka.softwaremill.com",
    "hello@softwaremill.com",
    "Apache License, Version 2.0",
    "http://www.apache.org/licenses/LICENSE-2.0.html"
  )
}

class BootzookaSwagger extends Swagger(Swagger.SpecVersion, "1.0.0", BootzookaSwagger.Info)