---
layout: default
title:  "Development tips"
---

Generally during development you'll need two processes:

* sbt running the backend server 
* grunt server which automatically picks up any changes

## Cloning

If you are planning to use Bootzooka as scaffolding for your own project, consider cloning the repo with `git clone --depth 1` in order to start the history with last commit. You can now switch to your origin repository of choice with: `git remote set-url origin https://repo.com/OTHERREPOSITORY.git`

## Useful sbt commands

* `renameProject` - replace Bootzooka with your custom name and adjust scala package names
* `compile` - compile the whole project
* `test` - run all the tests
* `project <sub-project-name>` - switch context to the given sub-project, then all the commands will be executed only for
that sub-project, this can be also achieved with e.g.: `<sub-project-name>/test`
* `~backend/re-start` - runs the backend server and waits for source code changes to automatically compile changed file and to reload it

## Database schema evolution

With Flyway, all you need to do is to put DDL script within bootzooka-backend/src/main/resources/db/migration/ directory. You have to obey the following [naming convention](http://flywaydb.org/documentation/migration/sql.html): `V#__your_arbitrary_description.sql` where `#` stands for *unique* version of your schema.

## Developing frontend without backend

If you'd like to work only on the frontend, without starting the backend, you can proxy requests to a working, remote backend instance. In `ui/Gruntfile.js` you need to edit the proxy settings and change the `proxies` option in `connect` to point to the backend instance, e.g.:

```
proxies: [ {
    context: '/rest/',
    host: 'my-backend.com',
    port: 80,
    headers: {
        'host': 'my-backend.com'
    }
} ]
```

## JSON

For serializing data to JSON the [Circe](https://github.com/travisbrown/circe) library is used. It relies on compile-time codec generation, instead of run-time reflection. If in your endpoint, you want to send a response to the client which corresponds to a case class, you need to:

1. make sure that the content of `JsonSupport` is in scope (e.g. by extending the trait itself or the more general `RoutesSupport`)
2. `import io.circe.generic.auto._` which will automatically generate a codec from the case class at compile-time
3. define an implicit `CanBeSerialized[T]` instance for the type `T` that you want to send. This is a feature of Bootzooka, not normally required, but included to make sure that you only send data that indeed should be sent (to avoid automatically serializing e.g. a list of `User` instances which contains the password hashes)

Of course, the existing endpoints (for managing users, getting the version) have all of that ready.

## Swagger

Bootzooka uses [swagger-akka-http:0.9.1](https://github.com/swagger-akka-http/swagger-akka-http) for compilation of `swagger.json` or `swagger.yaml` files on runtime. Swagger files are exposed to [http://localhost:8080/api-docs/swagger.yaml](http://localhost:8080/api-docs/swagger.yaml) or [http://localhost:8080/api-docs/swagger.json](http://localhost:8080/api-docs/swagger.json).

Routes are not added and described to Swagger files automatically, they has to be annotated first.

In order to describe a particular web method from backend API in Swagger you need to do next:
 
 1. extract route as def method to trait with annotations. Look at `VersionRoutes` and `VersionRoutesAnnotations` as example:
 ```
 trait VersionRoutesAnnotations {
   def getVersion: StandardRoute
 }
 ```
 
 2. annotate routes and used entities. See documentation in [swagger-akka-http:0.9.1](https://github.com/swagger-akka-http/swagger-akka-http) for compilation of `swagger.json` or `swagger.yaml` files. Swagger files are exposed to [http://localhost:8080/api-docs/swagger.yaml](http://localhost:8080/api-docs/swagger.yaml) or [http://localhost:8080/api-docs/swagger.json](http://localhost:8080/api-docs/swagger.json):
 ```
 @Api(value = "Version", description = "Operations about media build version",
   produces = "application/json", consumes = "application/json")
 @Path("/api/version")
 trait VersionRoutesAnnotations {
 
   @ApiOperation(httpMethod = "GET", response = classOf[VersionJson], value = "Returns an object which describes running version")
   @ApiResponses(Array(
     new ApiResponse(code = 500, message = "Internal Server Error"),
     new ApiResponse(code = 200, message = "OK", response = classOf[VersionJson])
   ))
   @Path("/")
   def getVersion: StandardRoute
 }
 
 @ApiModel(description = "Short description of the version of an object")
 case class VersionJson(
   @(ApiModelProperty @field)(value = "Build number") build: String,
   @(ApiModelProperty @field)(value = "The timestamp of the build") date: String
 )
```

3. add annotated route to val apiTypes in SwaggerDocApi class:
```
class SwaggerDocService(address: String, port: Int, system: ActorSystem) extends SwaggerHttpService with HasActorSystem {
 ...
  override val apiTypes = Seq( // add here routes in order to add to swagger
    ua.typeOf[VersionRoutes]
  )
  ...
}
```

4. run project. Check swagger at [http://localhost:8080/api-docs/swagger.yaml](http://localhost:8080/api-docs/swagger.yaml) or [http://localhost:8080/api-docs/swagger.json](http://localhost:8080/api-docs/swagger.json).

If the project is running locally, you might use [editor.swagger.io](http://editor.swagger.io/#!/) for testing purposes.
 