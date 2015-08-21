import java.text.SimpleDateFormat
import java.util.Date

import sbt._
import Keys._

import scala.util.Try
import scalariform.formatter.preferences._

val slf4jVersion = "1.7.9"
val logBackVersion = "1.1.2"
val scalaLoggingVersion = "3.1.0"
val json4sVersion = "3.2.11"
val seleniumVersion = "2.46.0"

val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
val logBackClassic = "ch.qos.logback" % "logback-classic" % logBackVersion
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
val loggingStack = Seq(slf4jApi, logBackClassic, scalaLogging)

val bugsnag = "com.bugsnag" % "bugsnag" % "1.2.8"

val typesafeConfig = "com.typesafe" % "config" % "1.2.1"

val jodaTime = "joda-time" % "joda-time" % "2.6"
val jodaConvert = "org.joda" % "joda-convert" % "1.7"
val jodaDependencies = Seq(jodaTime, jodaConvert)

val json4s = "org.json4s" %% "json4s-native" % json4sVersion

val javaxMailSun = "com.sun.mail" % "javax.mail" % "1.5.3"

val awaitility = "com.jayway.awaitility" % "awaitility-scala" % "1.6.3" % "test"

val slick = "com.typesafe.slick" %% "slick" % "3.0.0"
val h2 = "com.h2database" % "h2" % "1.3.176"
val postgres = "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
val hikari = "com.zaxxer" % "HikariCP-java6" % "2.3.8"
val flyway = "org.flywaydb" % "flyway-core" % "3.1"
val slickStack = Seq(slick, h2, postgres, hikari, flyway)

val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"
val scalatest = "org.scalatest" %% "scalatest" % "2.2.3" % "test"
val unitTestingStack = Seq(mockito, scalatest)

val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion % "test"
val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion % "test"
val fest = "org.easytesting" % "fest-assert" % "1.4" % "test"
val seleniumStack = Seq(seleniumJava, seleniumFirefox, fest)

val akkaHttpVersion = "1.0"
val akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % akkaHttpVersion
val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaHttpVersion % "test"
val akkaHttpSession = "com.softwaremill" %% "akka-http-session" % "0.1.4"
val akkaStack = Seq(akkaHttp, akkaHttpTestkit, akkaHttpSession)

val commonDependencies = unitTestingStack ++ loggingStack

name := "bootzooka"

// factor out common settings into a sequence
lazy val commonSettings = scalariformSettings ++ Seq(
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(PreserveSpaceBeforeArguments, true)
    .setPreference(CompactControlReadability, true)
    .setPreference(SpacesAroundMultiImports, false),
  organization := "com.softwaremill",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.11.6",
  scalacOptions ++= Seq("-unchecked", "-deprecation"),
  libraryDependencies ++= commonDependencies
)

def haltOnCmdResultError(result: Int) {
  if (result != 0) {
    throw new Exception("Build failed.")
  }
}

val updateNpm = baseDirectory map { bd =>
  println("Updating NPM dependencies")
  haltOnCmdResultError(Process("npm install", bd / ".." / "ui") !)
}

def gruntTask(taskName: String) = (baseDirectory, streams) map { (bd, s) =>
  val localGruntCommand = "./node_modules/.bin/grunt " + taskName
  def buildGrunt() = {
    Process(localGruntCommand, bd / ".." / "ui").!
  }
  println("Building with Grunt.js : " + taskName)
  haltOnCmdResultError(buildGrunt())
} dependsOn updateNpm

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(backend, ui)

lazy val backend: Project = (project in file("backend"))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(DeployToHeroku.settings)
  .settings(Revolver.settings)
  .settings(
    libraryDependencies ++= jodaDependencies ++ slickStack ++ akkaStack ++ Seq(json4s, javaxMailSun, typesafeConfig, bugsnag),
    buildInfoPackage := "com.softwaremill.bootzooka.version",
    buildInfoObject := "BuildInfo",
    buildInfoKeys := Seq[BuildInfoKey](
      BuildInfoKey.action("buildDate")(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())),
      // if the build is done outside of a git repository, we still want it to succeed
      BuildInfoKey.action("buildSha")(Try(Process("git rev-parse HEAD").!!.stripLineEnd).getOrElse("?"))),
    compile in Compile := {
      val compilationResult = (compile in Compile).value
      IO.touch(target.value / "compilationFinished")

      compilationResult
    },
    mainClass in Compile := Some("com.softwaremill.bootzooka.Main"),
    // We need to include the whole webapp, hence replacing the resource directory
    unmanagedResourceDirectories in Compile := {
      (unmanagedResourceDirectories in Compile).value ++ List(baseDirectory.value.getParentFile / ui.base.getName / "dist")
    },
    assemblyJarName in assembly := "bootzooka.jar",
    assembly <<= assembly dependsOn gruntTask("build")
  )

lazy val ui = (project in file("ui"))
  .settings(commonSettings: _*)
  .settings(test in Test <<= (test in Test) dependsOn gruntTask("test"))

lazy val uiTests = (project in file("ui-tests"))
  .settings(commonSettings: _*)
  .settings(
    parallelExecution := false,
    libraryDependencies ++= seleniumStack ++ Seq(awaitility),
    test in Test <<= (test in Test) dependsOn gruntTask("build")
  ) dependsOn backend

RenameProject.settings