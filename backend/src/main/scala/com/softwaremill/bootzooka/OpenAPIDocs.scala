package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.admin.VersionApi
import com.softwaremill.bootzooka.passwordreset.PasswordResetApi
import com.softwaremill.bootzooka.user.UserApi
import com.softwaremill.bootzooka.util.Endpoints
import ox.IO
import sttp.apispec.openapi.circe.yaml.*
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter

import java.nio.file.*

object OpenAPIDocs:
  val endpoints: Endpoints = UserApi.endpoints ++ PasswordResetApi.endpoints ++ VersionApi.endpoints

  @main def writeOpenAPISpec(path: String): Unit = IO.unsafe {
    val yaml = OpenAPIDocsInterpreter().toOpenAPI(endpoints, "Bootzooka", "1.0").toYaml
    Files.writeString(Paths.get(path), yaml)
    println(s"OpenAPI spec written to $path")
  }
