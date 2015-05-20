import java.text.SimpleDateFormat
import java.util.Date

import sbt._
import Keys._
import com.earldouglas.xsbtwebplugin.PluginKeys._
import sbt.ScalaVersion
import sbtassembly.Plugin._
import AssemblyKeys._
import sbtbuildinfo.Plugin._
import com.earldouglas.xsbtwebplugin.WebPlugin.webSettings

val slf4jVersion = "1.7.9"
val logBackVersion = "1.1.2"
val scalatraVersion = "2.3.1"
val scalaLoggingVersion = "3.1.0"
val jettyVersion = "9.2.6.v20141205"
val json4sVersion = "3.2.11"
val seleniumVersion = "2.44.0"

val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
val logBackClassic = "ch.qos.logback" % "logback-classic" % logBackVersion
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
lazy val loggingStack = Seq(slf4jApi, logBackClassic, scalaLogging)

val bugsnag = "com.bugsnag" % "bugsnag" % "1.2.8"

val typesafeConfig = "com.typesafe" % "config" % "1.2.1"

val commonsValidator = "commons-validator" % "commons-validator" % "1.4.0" exclude("commons-logging", "commons-logging")
val commonsLang = "org.apache.commons" % "commons-lang3" % "3.3.2"

val jetty = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion
val jettyContainer = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container"
val jettyTest = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "test"

val jodaTime = "joda-time" % "joda-time" % "2.6"
val jodaConvert = "org.joda" % "joda-convert" % "1.7"
lazy val jodaDependencies = Seq(jodaTime, jodaConvert)

val guava = "com.google.guava" % "guava" % "18.0"
val googleJsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.0"
lazy val commonDependencies = unitTestingStack ++ loggingStack ++ Seq(guava, googleJsr305)

val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
val scalatraScalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
val scalatraJson = "org.scalatra" %% "scalatra-json" % scalatraVersion
val scalatraSwagger = "org.scalatra" %% "scalatra-swagger" % scalatraVersion
val json4s = "org.json4s" %% "json4s-native" % json4sVersion
val scalatraAuth = "org.scalatra" %% "scalatra-auth" % scalatraVersion exclude("commons-logging", "commons-logging")
lazy val scalatraStack = Seq(scalatra, scalatraScalatest, scalatraJson, scalatraSwagger, json4s, scalatraAuth, commonsLang)

val javaxMail = "javax.mail" % "javax.mail-api" % "1.5.2"

val awaitility = "com.jayway.awaitility" % "awaitility-scala" % "1.6.3" % "test"

val slick = "com.typesafe.slick" %% "slick" % "3.0.0"
val h2 = "com.h2database" % "h2" % "1.3.176"
val c3p0 = "com.mchange" % "c3p0" % "0.9.5"
val flyway = "org.flywaydb" % "flyway-core" % "3.1"
lazy val slickOnH2Stack = Seq(slick, h2, c3p0, flyway)

val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"
val scalatest = "org.scalatest" %% "scalatest" % "2.2.3" % "test"
lazy val unitTestingStack = Seq(mockito, scalatest)

val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion % "test"
val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion % "test"
val fest = "org.easytesting" % "fest-assert" % "1.4" % "test"
lazy val seleniumStack = Seq(seleniumJava, seleniumFirefox, fest)

// If the scope is provided;test, as in scalatra examples then gen-idea generates the incorrect scope (test).
// As provided implies test, so is enough here.
lazy val servletApiProvided = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided" artifacts Artifact("javax.servlet", "jar", "jar")

name := "bootzooka"

// factor out common settings into a sequence
lazy val commonSettings = Seq(
  organization := "com.softwaremill",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.11.6",

  scalacOptions ++= Seq("-unchecked", "-deprecation"),
  classpathTypes ~= (_ + "orbit"),
  libraryDependencies ++= commonDependencies,

  parallelExecution := false
)

def haltOnCmdResultError(result: Int) {
  if (result != 0) {
    throw new Exception("Build failed.")
  }
}

val updateNpm = baseDirectory map { bd =>
  println("Updating NPM dependencies")
  haltOnCmdResultError(Process("npm install", bd / ".." / "bootzooka-ui") !)
}

def gruntTask(taskName: String) = (baseDirectory, streams) map { (bd, s) =>
  val localGruntCommand = "./node_modules/.bin/grunt " + taskName
  def buildGrunt() = {
    Process(localGruntCommand, bd / ".." / "bootzooka-ui").!
  }
  println("Building with Grunt.js : " + taskName)
  haltOnCmdResultError(buildGrunt())
} dependsOn updateNpm

lazy val bootzooka = (project in file(".")).
  settings(commonSettings: _*)
  .aggregate(backend, ui, dist)

lazy val backend: Project = (project in file("bootzooka-backend")).
  settings(commonSettings ++ webSettings: _*).
  settings(
    buildInfoSettings,
    libraryDependencies ++= jodaDependencies ++ slickOnH2Stack ++ scalatraStack ++
      Seq(jettyContainer, commonsValidator, javaxMail, typesafeConfig, servletApiProvided, bugsnag),
    sourceGenerators in Compile <+= buildInfo,
    buildInfoPackage := "com.softwaremill.bootzooka.version",
    buildInfoObject := "BootzookaBuildInfo",
    buildInfoKeys := Seq[BuildInfoKey](
      BuildInfoKey.action("buildDate")(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())),
      BuildInfoKey.action("buildSha")((Process("git rev-parse HEAD") !!).stripLineEnd)),
    Seq(
      artifactName := { (config: ScalaVersion, module: ModuleID, artifact: Artifact) =>
        "bootzooka." + artifact.extension // produces nice war name -> http://stackoverflow.com/questions/8288859/how-do-you-remove-the-scala-version-postfix-from-artifacts-builtpublished-wi
      },
      // We need to include the whole webapp, hence replacing the resource directory
      webappResources in Compile <<= baseDirectory { bd =>
        val restResources = bd.getParentFile / backend.base.getName / "src" / "main" / "webapp"
        val uiResources = bd.getParentFile / ui.base.getName / "dist" / "webapp"
        // "dist" may not yet exist, as it will be created by grunt later. However, we still need to include it, and
        // if it doesn't exist, SBT will complain
        if (!uiResources.exists() && !uiResources.mkdirs()) {
          throw new RuntimeException(s"$uiResources directory doesn't exist, and cannot be created!")
        }
        List(restResources, uiResources)
      },
      packageWar in DefaultConf <<= (packageWar in DefaultConf) dependsOn gruntTask("build"))
  )

lazy val ui = (project in file("bootzooka-ui")).
  settings(commonSettings: _*).
  settings(test in Test <<= (test in Test) dependsOn gruntTask("test"))

lazy val dist = (project in file("bootzooka-dist")).
  settings(commonSettings ++ assemblySettings: _*).
  settings(
    libraryDependencies += jetty,
    mainClass in assembly := Some("com.softwaremill.bootzooka.Bootzooka"),
    // We need to include the whole webapp, hence replacing the resource directory
    unmanagedResourceDirectories in Compile <<= baseDirectory { bd => {
      List(bd.getParentFile / backend.base.getName / "src" / "main", bd.getParentFile / ui.base.getName / "dist")
    }
    }
  ) dependsOn(ui, backend)

lazy val uiTests = (project in file("bootzooka-ui-tests")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= seleniumStack ++ Seq(awaitility, jettyTest, servletApiProvided),
    test in Test <<= (test in Test) dependsOn (Keys.`package` in Compile in backend)
  ) dependsOn dist