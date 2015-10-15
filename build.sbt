import java.text.SimpleDateFormat
import java.util.Date

import sbt._
import Keys._

import scala.util.Try
import scalariform.formatter.preferences._

val slf4jVersion = "1.7.12"
val logBackVersion = "1.1.3"
val scalaLoggingVersion = "3.1.0"
val slickVersion = "3.1.0"
val seleniumVersion = "2.46.0"
val circeVersion = "0.1.1"

val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
val logBackClassic = "ch.qos.logback" % "logback-classic" % logBackVersion
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
val loggingStack = Seq(slf4jApi, logBackClassic, scalaLogging)

val bugsnag = "com.bugsnag" % "bugsnag" % "1.2.8"

val typesafeConfig = "com.typesafe" % "config" % "1.2.1" // can't update to 1.3.0 to keep java <8 compat

val jodaTime = "joda-time" % "joda-time" % "2.8.2"
val jodaConvert = "org.joda" % "joda-convert" % "1.7"
val jodaDependencies = Seq(jodaTime, jodaConvert)

val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
val circeCore = "io.circe" %% "circe-core" % circeVersion
val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
val circeJawn = "io.circe" %% "circe-jawn" % circeVersion
val circe = Seq(circeCore, circeGeneric, circeJawn)

val javaxMailSun = "com.sun.mail" % "javax.mail" % "1.5.4"

val slick = "com.typesafe.slick" %% "slick" % slickVersion
val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
val h2 = "com.h2database" % "h2" % "1.3.176"
val postgres = "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
val flyway = "org.flywaydb" % "flyway-core" % "3.2.1"
val slickStack = Seq(slick, h2, postgres, slickHikari, flyway)

val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"
val scalatest = "org.scalatest" %% "scalatest" % "2.2.5" % "test"
val unitTestingStack = Seq(mockito, scalatest)

val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion % "test"
val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion % "test"
val seleniumStack = Seq(seleniumJava, seleniumFirefox)

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
  scalaVersion := "2.11.7",
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
    libraryDependencies ++= jodaDependencies ++ slickStack ++ akkaStack ++ circe ++ Seq(scalaXml, javaxMailSun, typesafeConfig, bugsnag),
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
    libraryDependencies ++= seleniumStack,
    test in Test <<= (test in Test) dependsOn gruntTask("build")
  ) dependsOn backend

RenameProject.settings