import sbtbuildinfo.BuildInfoKey.action
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoOption}

import scala.util.Try
import scala.sys.process.Process
import complete.DefaultParsers._

val password4jVersion = "1.8.4"
val sttpVersion = "4.0.27"
val tapirVersion = "1.13.32"
val oxVersion = "1.0.8"
val otelVersion = "1.66.0"
val otelInstrumentationVersion = "2.17.1-alpha"

val dbDependencies = Seq(
  "com.augustnagro" %% "magnum" % "1.3.1", // Scala DB client
  "org.postgresql" % "postgresql" % "42.7.13", // JDBC driver
  "com.zaxxer" % "HikariCP" % "7.1.0", // connection pool
  "org.flywaydb" % "flyway-database-postgresql" % "13.8.0" // database migrations
)

val httpDependencies = Seq(
  "com.softwaremill.sttp.client4" %% "core" % sttpVersion, // HTTP client
  "com.softwaremill.sttp.client4" %% "slf4j-backend" % sttpVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % tapirVersion, // HTTP server, using the synchronous Netty backend
  "com.softwaremill.sttp.tapir" %% "tapir-files" % tapirVersion // serving static files
)

val observabilityDependencies = Seq(
  "com.softwaremill.sttp.client4" %% "opentelemetry-backend" % sttpVersion, // OTEL <-> sttp integration
  "com.softwaremill.sttp.tapir" %% "tapir-opentelemetry-metrics" % tapirVersion, // OTEL <-> Tapir integration
  "com.softwaremill.sttp.tapir" %% "tapir-opentelemetry-tracing" % tapirVersion, // OTEL <-> Tapir integration
  "com.softwaremill.ox" %% "otel-context" % oxVersion, // OTEL context propagation in Ox scopes
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % otelVersion exclude ("io.opentelemetry", "opentelemetry-exporter-sender-okhttp"),
  "io.opentelemetry" % "opentelemetry-exporter-sender-jdk" % otelVersion,
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % otelVersion,
  "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry-java17" % otelInstrumentationVersion, // OTEL JVM metrics
  "io.opentelemetry.instrumentation" % "opentelemetry-logback-appender-1.0" % otelInstrumentationVersion // send logs via OTEL
)

val jsonDependencies = Seq(
  "com.softwaremill.sttp.client4" %% "jsoniter" % sttpVersion, // main JSON library
  "com.softwaremill.sttp.tapir" %% "tapir-jsoniter-scala" % tapirVersion, // Tapir <-> jsoniter integration
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.41.2" // automatic codec derivation
)

val loggingDependencies = Seq(
  "ch.qos.logback" % "logback-classic" % "1.6.4", // main logging library
  "org.slf4j" % "jul-to-slf4j" % "2.0.20", // forward e.g. OTEL and Magnum logs which use JUL to SLF4J
  "com.softwaremill.ox" %% "mdc-logback" % oxVersion, // support MDCs which propagate within Ox scopes
  "org.slf4j" % "slf4j-jdk-platform-logging" % "2.0.20" % Runtime // route Java's platform logging (separate from JUL) to SLF4J
)

val configDependencies = Seq(
  "com.github.pureconfig" %% "pureconfig-core" % "0.17.10"
)

val baseDependencies = Seq(
  "com.softwaremill.ox" %% "core" % oxVersion, // concurrency, streaming & error handling utilities
  "com.softwaremill.quicklens" %% "quicklens" % "1.9.15",
  "com.softwaremill.macwire" %% "macros" % "2.6.7" % Provided // compile-time generation of dependency tree (DI replacement)
)

val apiDocsDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion // Swagger UI for the HTTP API
)

val securityDependencies = Seq(
  "com.password4j" % "password4j" % password4jVersion // password hashing
)

val emailDependencies = Seq(
  "com.sun.mail" % "javax.mail" % "1.6.2" // JavaMail API when emails are sent directly
)

val testingDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.2.20",
  "com.opentable.components" % "otj-pg-embedded" % "1.1.3", // embedded PostgreSQL for tests
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub4-server" % tapirVersion, // integration testing HTTP endpoints without starting a server
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-client4" % tapirVersion // interpreting endpoint descriptions as HTTP requests
).map(_ % Test)

val allBackendDependencies = baseDependencies ++ testingDependencies ++ loggingDependencies ++ configDependencies ++
  dbDependencies ++ httpDependencies ++ jsonDependencies ++ apiDocsDependencies ++ observabilityDependencies ++
  securityDependencies ++ emailDependencies

// other constants

val mainClassName = "com.softwaremill.bootzooka.Main"
val uiProjectName = "ui"

// custom tasks & settings

val uiDirectory = settingKey[File]("Path to the ui project directory") // capturing as a setting to run yarn during the build
val updateYarn = taskKey[Unit]("Update yarn") // separate task so that update is run once per build
val yarnTask = inputKey[Unit]("Run yarn with arguments")
val copyWebapp = taskKey[Seq[File]]("Copy webapp")
val generateOpenAPIDescription = taskKey[Unit]("Generate the OpenAPI description for the HTTP API")
val reStart = taskKey[Unit]("(Re)starts the backend in the background; use `~backend/reStart` to restart on source changes")
val reStop = taskKey[Unit]("Stops the backend started with reStart")

def haltOnCmdResultError(result: Int): Unit = if (result != 0) { throw new Exception("Build failed.") }

organization := "com.softwaremill.bootzooka"
scalaVersion := "3.9.0"
// version
git.formattedShaVersion := {
  val base = git.baseVersion.?.value
  val suffix = git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value, Some("dirty"))
  git.gitHeadCommit.value.map(sha => git.defaultFormatShaVersion(base, sha.take(7), suffix))
}
version := git.gitDescribedVersion.value.getOrElse(git.formattedShaVersion.value.getOrElse("latest"))

// defining the updateYarn task in the global scope so that it's always run at most once per build
// otherwise, two `yarn install` might end up running concurrently, which leads to errors
ThisBuild / uiDirectory := (ThisBuild / baseDirectory).value / uiProjectName
ThisBuild / updateYarn := Def.uncached {
  val log = (ThisBuild / streams).value.log
  val uiDir = (ThisBuild / uiDirectory).value
  log.info("Updating npm/yarn dependencies")
  haltOnCmdResultError(Process("yarn install", uiDir).!)
}
ThisBuild / yarnTask := Def.uncached {
  (ThisBuild / updateYarn).value
  val taskName = spaceDelimited("<arg>").parsed.mkString(" ")
  // use the "docker" mode to use the .env.docker file, which assumes the UI & API is available at
  // the same domain
  val localYarnCommand = "yarn " + taskName + " --mode docker"
  val log = (ThisBuild / streams).value.log
  val uiDir = (ThisBuild / uiDirectory).value
  def runYarnTask() = Process(localYarnCommand, uiDir).!
  log.info("Running yarn task: " + taskName)
  haltOnCmdResultError(runYarnTask())
}

lazy val root: Project = rootProject
  .settings(name := "bootzooka")
  // in sbt 2, bare settings are applied to all subprojects, while the rename should only run once, for the root project
  .settings(RenameProject.settings)
  .aggregate(backend, ui, docker)

lazy val backend: Project = (project in file("backend"))
  .settings(
    libraryDependencies ++= allBackendDependencies,
    // in case the backend jar is used outside of Docker, specifying the main class
    Compile / mainClass := Some(mainClassName),
    // generates the target/openapi.yaml file which is then used by the UI to generate service stubs
    generateOpenAPIDescription := Def.uncached {
      val targetDir = baseDirectory.value / "target"
      IO.createDirectory(targetDir)
      val targetPath = (targetDir / "openapi.yaml").toString
      val converter = fileConverter.value
      val classpath = (Compile / fullClasspath).value.map(entry => converter.toPath(entry.data))
      (Compile / run / runner).value
        .run("com.softwaremill.bootzooka.writeOpenAPIDescription", classpath, Seq(targetPath), streams.value.log)
        .get
    },
    // used by backend-start.sh, to restart the application when sources change; the OpenAPI spec needs to be
    // regenerated so that the UI updates accordingly
    reStop := Def.uncached {
      val jobService = bgJobService.value
      val thisProject = Select(thisProjectRef.value)
      jobService.jobs.filter(_.spawningTask.scope.project == thisProject).foreach { job =>
        jobService.stop(job)
        scala.util.Try(jobService.waitFor(job))
      }
    },
    reStart := Def.uncached(Def.sequential(reStop, generateOpenAPIDescription, (Compile / bgRun).toTask("").map(_ => ())).value),
    // needed so that a ctrl+c issued when running the backend from the sbt console properly interrupts the application
    run / fork := true,
    // use sbt-tpolecat, but without fatal warnings
    scalacOptions ~= (_.filterNot(Set("-Xfatal-warnings"))),
    // silence unused assertion results warnings in tests
    Test / scalacOptions += "-Wconf:msg=unused value of type org.scalatest.Assertion:s",
    Test / scalacOptions += "-Wconf:msg=unused value of type org.scalatest.compatible.Assertion:s"
  )
  // the build information is displayed in the UI, and provided by an API endpoint
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      action("lastCommitHash") {
        import scala.sys.process._
        // if the build is done outside of a git repository, we still want it to succeed
        Try("git rev-parse HEAD".!!.trim).getOrElse("?")
      }
    ),
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.ToMap,
    buildInfoPackage := "com.softwaremill.bootzooka.version",
    buildInfoObject := "BuildInfo"
  )

lazy val ui: Project = (project in file(uiProjectName))
  .settings(
    Test / test := {
      yarnTask.toTask(" test:ci").value
      (Test / test).evaluated
    },
    Test / testFull := Def.uncached((Test / testFull).dependsOn(yarnTask.toTask(" test:ci")).value)
  )
  .settings(cleanFiles += baseDirectory.value / "dist")

lazy val docker: Project = (project in file("docker"))
  .settings(
    copyWebapp := Def.uncached {
      val source = uiDirectory.value / "dist"
      val target = (Compile / resourceManaged).value / "webapp"
      streams.value.log.info(s"Copying the webapp resources from $source to $target")
      IO.delete(target)
      IO.copyDirectory(source, target)
      (target ** "*").filter(_.isFile).get()
    },
    // There are no source files in this project, we're just using it to generate a jar with the UI files.
    // To do that, we need to first generate the OpenAPI spec, build the UI, and copy the UI files as resources.
    Compile / resourceGenerators += Def.sequential(backend / generateOpenAPIDescription, yarnTask.toTask(" build"), copyWebapp).taskValue,
    // Docker settings
    Compile / mainClass := Some(mainClassName),
    dockerExposedPorts := Seq(8080),
    dockerBaseImage := "eclipse-temurin:21",
    Docker / packageName := "bootzooka",
    dockerUsername := Some("softwaremill"),
    dockerUpdateLatest := true
  )
  .dependsOn(backend, ui)
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaServerAppPackaging)
