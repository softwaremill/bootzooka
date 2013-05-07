package com.softwaremill.bootzooka.rest

import org.scalatra.swagger.{JacksonSwaggerBase, Swagger}

import org.scalatra.ScalatraServlet
import org.json4s.{DefaultFormats, Formats}

class SwaggerApiDoc(val swagger: Swagger) extends ScalatraServlet with JacksonSwaggerBase {
  override implicit val jsonFormats: Formats = DefaultFormats
}

class BootzookaSwagger extends Swagger("1.0", "1")
