package com.softwaremill.bootzooka.api.swagger

import com.softwaremill.bootzooka.api.Mappable
import com.typesafe.scalalogging.LazyLogging
import org.scalatra.ScalatraServlet
import org.scalatra.swagger.{ApiInfo, NativeSwaggerBase, Swagger}

class SwaggerServlet(implicit val swagger: Swagger) extends ScalatraServlet with NativeSwaggerBase with LazyLogging with Mappable {

  override def mappingPath: String = "api-docs"
}

object AppSwagger {

  val Info = ApiInfo(
    "Web API",
    "Docs for the web API",
    "http://bootzooka.softwaremill.com",
    "hello@softwaremill.com",
    "Apache License, Version 2.0",
    "http://www.apache.org/licenses/LICENSE-2.0.html"
  )
}

class AppSwagger extends Swagger(Swagger.SpecVersion, "1.0.0", AppSwagger.Info)