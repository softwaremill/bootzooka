package com.softwaremill.bootzooka.version

import javax.ws.rs.Path

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.softwaremill.bootzooka.common.api.RoutesSupport
import com.softwaremill.bootzooka.version.BuildInfo._
import io.circe.generic.auto._
import io.swagger.annotations.{ApiResponse, _}

import scala.annotation.meta.field

trait VersionRoutes extends RoutesSupport with VersionRoutesAnnotations {

  implicit val versionJsonCbs = CanBeSerialized[VersionJson]

  val versionRoutes = pathPrefix("version") {
    pathEndOrSingleSlash {
      getVersion
    }
  }

  def getVersion: Route =
    complete {
      VersionJson(buildSha.substring(0, 6), buildDate)
    }
}

@Api(
  value = "Version",
  produces = "application/json",
  consumes = "application/json"
)
@Path("api/version")
trait VersionRoutesAnnotations {

  @ApiOperation(
    httpMethod = "GET",
    response = classOf[VersionJson],
    value = "Returns an object which describes running version"
  )
  @ApiResponses(
    Array(
      new ApiResponse(code = 500, message = "Internal Server Error"),
      new ApiResponse(code = 200, message = "OK", response = classOf[VersionJson])
    )
  )
  @Path("/")
  def getVersion: Route
}

@ApiModel(description = "Short description of the version of an object")
case class VersionJson(
    @(ApiModelProperty @field)(value = "Build number") build: String,
    @(ApiModelProperty @field)(value = "The timestamp of the build") date: String
)
