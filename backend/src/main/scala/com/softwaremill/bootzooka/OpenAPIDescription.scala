package com.softwaremill.bootzooka

import sttp.apispec.openapi.circe.yaml.*
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter

import java.nio.file.*

object OpenAPIDescription:
  val Title = "Bootzooka"
  val Version = "1.0"

@main def writeOpenAPIDescription(path: String): Unit =
  val yaml = OpenAPIDocsInterpreter().toOpenAPI(Dependencies.endpointsForDocs, OpenAPIDescription.Title, OpenAPIDescription.Version).toYaml
  Files.writeString(Paths.get(path), yaml)
  println(s"OpenAPI description document written to: $path")
