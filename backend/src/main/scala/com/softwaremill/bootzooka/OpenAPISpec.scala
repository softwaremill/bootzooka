package com.softwaremill.bootzooka

import ox.IO
import sttp.apispec.openapi.circe.yaml.*
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter

import java.nio.file.*

object OpenAPISpec:
  val Title = "Bootzooka"
  val Version = "1.0"

@main def writeOpenAPISpec(path: String): Unit = IO.unsafe {
  val yaml = OpenAPIDocsInterpreter().toOpenAPI(Dependencies.endpoints, OpenAPISpec.Title, OpenAPISpec.Version).toYaml
  Files.writeString(Paths.get(path), yaml)
  println(s"OpenAPI spec written to $path")
}
