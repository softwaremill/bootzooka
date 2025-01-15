import sbtbuildinfo.BuildInfoKey.action
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoOption}

import sbt._
import Keys._

import scala.util.Try
import scala.sys.process.Process
import complete.DefaultParsers._

val password4jVersion = "1.8.2"
val sttpVersion = "3.10.2"
val tapirVersion = "1.11.12"
val oxVersion = "0.5.1"
val otelVersion = "1.46.0"
val otelInstrumentationVersion = "2.8.0-alpha"

val dbDependencies = Seq(
  "com.augustnagro" %% "magnum" % "1.3.1",
  "org.postgresql" % "postgresql" % "42.7.5",
  "com.zaxxer" % "HikariCP" % "6.2.1",
  "org.flywaydb" % "flyway-database-postgresql" % "10.20.1"
)

val httpDependencies = Seq(
  "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "slf4j-backend" % sttpVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-files" % tapirVersion
)

val observabilityDependencies = Seq(
  "com.softwaremill.sttp.client3" %% "opentelemetry-metrics-backend" % sttpVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-opentelemetry-metrics" % tapirVersion,
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % otelVersion,
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % otelVersion,
  "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry-java8" % otelInstrumentationVersion,
  "io.opentelemetry.instrumentation" % "opentelemetry-logback-appender-1.0" % otelInstrumentationVersion
)

val jsonDependencies = Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.33.0",
  "com.softwaremill.sttp.tapir" %% "tapir-jsoniter-scala" % tapirVersion,
  "com.softwaremill.sttp.client3" %% "jsoniter" % sttpVersion
)

val loggingDependencies = Seq(
  "ch.qos.logback" % "logback-classic" % "1.5.16",
  "org.slf4j" % "jul-to-slf4j" % "2.0.16", // forward e.g. otel logs which use JUL to SLF4J
  "com.softwaremill.ox" %% "mdc-logback" % oxVersion,
  "org.slf4j" % "slf4j-jdk-platform-logging" % "2.0.16" % Runtime,
  "org.codehaus.janino" % "janino" % "3.1.12" % Runtime,
  "net.logstash.logback" % "logstash-logback-encoder" % "8.0" % Runtime
)

val configDependencies = Seq(
  "com.github.pureconfig" %% "pureconfig-core" % "0.17.8"
)

val baseDependencies = Seq(
  "com.softwaremill.ox" %% "core" % oxVersion,
  "com.softwaremill.quicklens" %% "quicklens" % "1.9.11",
  "com.softwaremill.macwire" %% "macros" % "2.6.4" % Provided
)

val apiDocsDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion
)

val securityDependencies = Seq(
  "com.password4j" % "password4j" % password4jVersion
)

val emailDependencies = Seq(
  "com.sun.mail" % "javax.mail" % "1.6.2" exclude ("javax.activation", "activation")
)

val testingDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "com.opentable.components" % "otj-pg-embedded" % "1.1.0" % Test
)

lazy val uiProjectName = "ui"
lazy val uiDirectory = settingKey[File]("Path to the ui project directory")
lazy val updateYarn = taskKey[Unit]("Update yarn")
lazy val yarnTask = inputKey[Unit]("Run yarn with arguments")
lazy val copyWebapp = taskKey[Unit]("Copy webapp")
lazy val generateOpenAPIDescription = taskKey[Unit]("Generate the OpenAPI description for the HTTP API")

lazy val commonSettings = Seq(
  organization := "com.softwaremill.bootzooka",
  scalaVersion := "3.5.2",
  uiDirectory := (ThisBuild / baseDirectory).value / uiProjectName,
  updateYarn := {
    streams.value.log("Updating npm/yarn dependencies")
    haltOnCmdResultError(Process("yarn install", uiDirectory.value).!)
  },
  yarnTask := {
    val taskName = spaceDelimited("<arg>").parsed.mkString(" ")
    updateYarn.value
    val localYarnCommand = "yarn " + taskName
    def runYarnTask() = Process(localYarnCommand, uiDirectory.value).!
    streams.value.log("Running yarn task: " + taskName)
    haltOnCmdResultError(runYarnTask())
  }
)

lazy val buildInfoSettings = Seq(
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

lazy val fatJarSettings = Seq(
  assembly / assemblyJarName := "bootzooka.jar",
  assembly := assembly.dependsOn(copyWebapp).value,
  assembly / assemblyMergeStrategy := {
    // SwaggerUI: https://tapir.softwaremill.com/en/latest/docs/openapi.html#using-swaggerui-with-sbt-assembly
    case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") => MergeStrategy.singleOrError
    case PathList("META-INF", "resources", "webjars", "swagger-ui", _*)               => MergeStrategy.singleOrError
    // other
    case PathList(ps @ _*) if ps.last endsWith "io.netty.versions.properties" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith "pom.properties"               => MergeStrategy.discard
    case PathList(ps @ _*) if ps.last endsWith "module-info.class"            => MergeStrategy.discard
    case PathList(ps @ _*) if ps.last endsWith "okio.kotlin_module"           => MergeStrategy.discard
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

lazy val dockerSettings = Seq(
  dockerExposedPorts := Seq(8080),
  dockerBaseImage := "eclipse-temurin:21",
  Docker / packageName := "bootzooka",
  dockerUsername := Some("softwaremill"),
  dockerUpdateLatest := true,
  Docker / stage := (Docker / stage).dependsOn(copyWebapp).value,
  Docker / version := git.gitDescribedVersion.value.getOrElse(git.formattedShaVersion.value.getOrElse("latest")),
  git.uncommittedSignifier := Some("dirty"),
  ThisBuild / git.formattedShaVersion := {
    val base = git.baseVersion.?.value
    val suffix = git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value, git.uncommittedSignifier.value)
    git.gitHeadCommit.value.map { sha =>
      git.defaultFormatShaVersion(base, sha.take(7), suffix)
    }
  }
)

def haltOnCmdResultError(result: Int): Unit = if (result != 0) {
  throw new Exception("Build failed.")
}

def now(): String = {
  import java.text.SimpleDateFormat
  import java.util.Date
  new SimpleDateFormat("yyyy-MM-dd-hhmmss").format(new Date())
}

lazy val rootProject = (project in file("."))
  .settings(commonSettings)
  .settings(name := "bootzooka")
  .aggregate(backend, ui)

lazy val backend: Project = (project in file("backend"))
  .settings(
    libraryDependencies ++= baseDependencies ++ testingDependencies ++ loggingDependencies ++
      configDependencies ++ dbDependencies ++ httpDependencies ++ jsonDependencies ++
      apiDocsDependencies ++ observabilityDependencies ++ securityDependencies ++ emailDependencies,
    Compile / mainClass := Some("com.softwaremill.bootzooka.Main"),
    copyWebapp := {
      val source = uiDirectory.value / "build"
      val target = (Compile / classDirectory).value / "webapp"
      streams.value.log.info(s"Copying the webapp resources from $source to $target")
      IO.copyDirectory(source, target)
    },
    copyWebapp := copyWebapp
      .dependsOn(
        Def
          .sequential(
            generateOpenAPIDescription,
            yarnTask.toTask(" build")
          )
      )
      .value,
    generateOpenAPIDescription := Def.taskDyn {
      val log = streams.value.log
      val targetPath = ((Compile / target).value / "openapi.yaml").toString
      Def.task {
        (Compile / runMain).toTask(s" com.softwaremill.bootzooka.writeOpenAPIDescription $targetPath").value
      }
    }.value,
    // needed so that a ctrl+c issued when running the backend from the sbt console properly interrupts the application
    run / fork := true,
    scalacOptions ++= List("-Wunused:all", "-Wvalue-discard")
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(Revolver.settings)
  .settings(buildInfoSettings)
  .settings(fatJarSettings)
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaServerAppPackaging)
  .settings(dockerSettings)

lazy val ui = (project in file(uiProjectName))
  .settings(commonSettings)
  .settings(Test / test := (Test / test).dependsOn(yarnTask.toTask(" test:ci")).value)
  .settings(cleanFiles += baseDirectory.value / "build")

RenameProject.settings
