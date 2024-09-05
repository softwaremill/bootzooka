import sbtbuildinfo.BuildInfoKey.action
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoOption}
import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings

import sbt._
import Keys._

import scala.util.Try
import scala.sys.process.Process
import complete.DefaultParsers._

val password4jVersion = "1.8.2"
val sttpVersion = "3.9.8"
val tapirVersion = "1.11.2"
val oxVersion = "0.3.5"

val dbDependencies = Seq(
  "com.augustnagro" %% "magnum" % "1.2.1",
  "org.postgresql" % "postgresql" % "42.7.4",
  "com.zaxxer" % "HikariCP" % "5.1.0",
  "org.flywaydb" % "flyway-database-postgresql" % "10.17.3"
)

val httpDependencies = Seq(
  "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "slf4j-backend" % sttpVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-files" % tapirVersion
)

val monitoringDependencies = Seq(
  "com.softwaremill.sttp.client3" %% "opentelemetry-metrics-backend" % sttpVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-opentelemetry-metrics" % tapirVersion,
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.41.0"
)

val jsonDependencies = Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.9",
  "com.softwaremill.sttp.tapir" %% "tapir-jsoniter-scala" % tapirVersion,
  "com.softwaremill.sttp.client3" %% "jsoniter" % sttpVersion
)

val loggingDependencies = Seq(
  "ch.qos.logback" % "logback-classic" % "1.5.7",
  "com.softwaremill.ox" %% "mdc-logback" % oxVersion,
  "org.codehaus.janino" % "janino" % "3.1.12" % Runtime,
  "net.logstash.logback" % "logstash-logback-encoder" % "8.0" % Runtime
)

val configDependencies = Seq(
  "com.github.pureconfig" %% "pureconfig-core" % "0.17.7"
)

val baseDependencies = Seq(
  "com.softwaremill.ox" %% "core" % oxVersion,
  "com.softwaremill.quicklens" %% "quicklens" % "1.9.8"
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

val scalatest = "org.scalatest" %% "scalatest" % "3.2.19" % Test

val unitTestingStack = Seq(scalatest)

val embeddedPostgres = "com.opentable.components" % "otj-pg-embedded" % "1.1.0" % Test
val dbTestingStack = Seq(embeddedPostgres)

val commonDependencies = baseDependencies ++ unitTestingStack ++ loggingDependencies ++ configDependencies

lazy val uiProjectName = "ui"
lazy val uiDirectory = settingKey[File]("Path to the ui project directory")
lazy val updateYarn = taskKey[Unit]("Update yarn")
lazy val yarnTask = inputKey[Unit]("Run yarn with arguments")
lazy val copyWebapp = taskKey[Unit]("Copy webapp")

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  organization := "com.softwaremill.bootzooka",
  scalaVersion := "3.3.3",
  libraryDependencies ++= commonDependencies,
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
  },
  autoCompilerPlugins := true,
  addCompilerPlugin("com.softwaremill.ox" %% "plugin" % "0.3.5"),
  Compile / scalacOptions += "-P:requireIO:javax.mail.MessagingException"
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
  Compile / packageBin := (Compile / packageBin).dependsOn(copyWebapp).value,
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
    libraryDependencies ++= dbDependencies ++ httpDependencies ++ jsonDependencies ++ apiDocsDependencies ++ monitoringDependencies ++ dbTestingStack ++ securityDependencies ++ emailDependencies,
    Compile / mainClass := Some("com.softwaremill.bootzooka.Main"),
    copyWebapp := {
      val source = uiDirectory.value / "build"
      val target = (Compile / classDirectory).value / "webapp"
      streams.value.log.info(s"Copying the webapp resources from $source to $target")
      IO.copyDirectory(source, target)
    },
    copyWebapp := copyWebapp.dependsOn(yarnTask.toTask(" build")).value,
    // needed so that a ctrl+c issued when running the backend from the sbt console properly interrupts the application
    run / fork := true
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
