import sbtbuildinfo.BuildInfoKey.action
import sbtbuildinfo.BuildInfoKeys.{buildInfoKeys, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.{BuildInfoKey, BuildInfoOption}

import sbt._
import Keys._

import scala.util.Try
import scala.sys.process.Process
import complete.DefaultParsers._

val seleniumVersion = "2.53.0"
val doobieVersion = "0.7.0"
val http4sVersion = "0.20.6"
val circeVersion = "0.11.1"
val tsecVersion = "0.1.0"
val sttpVersion = "1.6.3"
val prometheusVersion = "0.6.0"
val tapirVersion = "0.9.0"

val dbDependencies = Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.flywaydb" % "flyway-core" % "5.2.4"
)

val httpDependencies = Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-prometheus-metrics" % http4sVersion,
  "com.softwaremill.sttp" %% "async-http-client-backend-monix" % sttpVersion,
  "com.softwaremill.tapir" %% "tapir-http4s-server" % tapirVersion
)

val monitoringDependencies = Seq(
  "io.prometheus" % "simpleclient" % prometheusVersion,
  "io.prometheus" % "simpleclient_hotspot" % prometheusVersion,
  "com.softwaremill.sttp" %% "prometheus-backend" % sttpVersion
)

val jsonDependencies = Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-java8" % circeVersion,
  "com.softwaremill.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp" %% "circe" % sttpVersion
)

val loggingDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.softwaremill.correlator" %% "monix-logback-http4s" % "0.1.1"
)

val configDependencies = Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.11.1"
)

val baseDependencies = Seq(
  "io.monix" %% "monix" % "3.0.0-RC3",
  "com.softwaremill.common" %% "tagging" % "2.2.1",
  "com.softwaremill.quicklens" %% "quicklens" % "1.4.12"
)

val apiDocsDependencies = Seq(
  "com.softwaremill.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
  "com.softwaremill.tapir" %% "tapir-swagger-ui-http4s" % tapirVersion
)

val securityDependencies = Seq(
  "io.github.jmcardon" %% "tsec-password" % tsecVersion,
  "io.github.jmcardon" %% "tsec-cipher-jca" % tsecVersion
)

val emailDependencies = Seq(
  "javax.mail" % "javax.mail-api" % "1.6.2"
)

val scalatest = "org.scalatest" %% "scalatest" % "3.0.8" % "test"
val unitTestingStack = Seq(scalatest)

val embeddedPostgres = "com.opentable.components" % "otj-pg-embedded" % "0.13.1"
val dbTestingStack = Seq(embeddedPostgres)

val commonDependencies = baseDependencies ++ unitTestingStack ++ loggingDependencies ++ configDependencies

lazy val updateNpm = taskKey[Unit]("Update npm")
lazy val npmTask = inputKey[Unit]("Run npm with arguments")

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  organization := "com.softwaremill.bootzooka",
  scalaVersion := "2.12.8",
  libraryDependencies ++= commonDependencies,
  updateNpm := {
    println("Updating npm dependencies")
    haltOnCmdResultError(Process("npm install", baseDirectory.value / ".." / "ui").!)
  },
  npmTask := {
    val taskName = spaceDelimited("<arg>").parsed.mkString(" ")
    updateNpm.value
    val localNpmCommand = "npm " + taskName
    def buildWebpack() =
      Process(localNpmCommand, baseDirectory.value / ".." / "ui").!
    println("Building with Webpack : " + taskName)
    haltOnCmdResultError(buildWebpack())
  }
)

def haltOnCmdResultError(result: Int) {
  if (result != 0) {
    throw new Exception("Build failed.")
  }
}

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "bootzooka",
    herokuFatJar in Compile := Some((assemblyOutputPath in backend in assembly).value),
    deployHeroku in Compile := ((deployHeroku in Compile) dependsOn (assembly in backend)).value
  )
  .aggregate(backend, ui)

lazy val backend: Project = (project in file("backend"))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(Revolver.settings)
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
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.ToMap,
    buildInfoPackage := "com.softwaremill.bootzooka.version",
    buildInfoObject := "BuildInfo"
  )
  .settings(
    libraryDependencies ++= dbDependencies ++ httpDependencies ++ jsonDependencies ++ apiDocsDependencies ++ monitoringDependencies ++ dbTestingStack ++ securityDependencies ++ emailDependencies,
    compile in Compile := {
      val compilationResult = (compile in Compile).value
      IO.touch(target.value / "compilationFinished")

      compilationResult
    },
    mainClass in Compile := Some("com.softwaremill.bootzooka.Main"),
    // We need to include the whole webapp, hence replacing the resource directory
    unmanagedResourceDirectories in Compile := {
      (unmanagedResourceDirectories in Compile).value ++ List(
        baseDirectory.value.getParentFile / ui.base.getName / "dist"
      )
    },
    assemblyJarName in assembly := "bootzooka.jar",
    assembly := assembly.dependsOn(npmTask.toTask(" run build")).value,
    assemblyMergeStrategy in assembly := {
      case PathList(ps @ _*) if ps.last endsWith "io.netty.versions.properties" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith "pom.properties" => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

lazy val ui = (project in file("ui"))
  .settings(commonSettings: _*)
  .settings(test in Test := (test in Test).dependsOn(npmTask.toTask(" run test")).value)

//val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion % "test"
//val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion % "test"
//val seleniumDependencies = Seq(seleniumJava, seleniumFirefox)
//
//lazy val uiTests = (project in file("ui-tests"))
//  .settings(commonSettings: _*)
//  .settings(
//    parallelExecution := false,
//    libraryDependencies ++= seleniumDependencies,
//    test in Test := (test in Test).dependsOn(npmTask.toTask(" run build")).value
//  ) dependsOn backend

RenameProject.settings
