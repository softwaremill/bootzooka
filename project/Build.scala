import sbt._
import Keys._
import net.virtualvoid.sbt.graph.Plugin._
import com.earldouglas.xsbtwebplugin.PluginKeys._
import sbt.ScalaVersion
import sbtassembly.Plugin._
import AssemblyKeys._

object BuildSettings {

  val buildSettings = Seq(

    organization := "com.softwaremill",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.11.4",

    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    classpathTypes ~= (_ + "orbit"),
    libraryDependencies ++= Dependencies.testingDependencies,
    libraryDependencies ++= Dependencies.logging,
    libraryDependencies ++= Seq(Dependencies.guava, Dependencies.googleJsr305),

    parallelExecution := false
  )

}

object Dependencies {

  val slf4jVersion = "1.7.9"
  val logBackVersion = "1.1.2"
  val scalatraVersion = "2.3.0"
  val rogueVersion = "2.4.0"
  val scalaLoggingVersion = "2.1.2"
  val jettyVersion = "9.2.6.v20141205"
  val json4sVersion = "3.2.10" // the latest version that works with Swagger, see https://github.com/scalatra/scalatra/issues/446

  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
  val logBackClassic = "ch.qos.logback" % "logback-classic" % logBackVersion
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging-slf4j" % scalaLoggingVersion

  val logging = Seq(slf4jApi, logBackClassic, scalaLogging)

  val typesafeConfig = "com.typesafe" % "config" % "1.2.1"

  val guava = "com.google.guava" % "guava" % "18.0"
  val googleJsr305 = "com.google.code.findbugs" % "jsr305" % "3.0.0"

  val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
  val scalatraScalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
  val scalatraJson = "org.scalatra" %% "scalatra-json" % scalatraVersion
  val scalatraSwagger = "org.scalatra" %% "scalatra-swagger" % scalatraVersion
  val json4s = "org.json4s"   %% "json4s-native" % json4sVersion
  val scalatraAuth = "org.scalatra" %% "scalatra-auth" % scalatraVersion  exclude("commons-logging", "commons-logging")

  val jodaTime = "joda-time" % "joda-time" % "2.6"
  val jodaConvert = "org.joda" % "joda-convert" % "1.7"

  val commonsValidator = "commons-validator" % "commons-validator" % "1.4.0" exclude("commons-logging", "commons-logging")
  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.3.2"

  val jetty = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion
  val jettyContainer = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container"
  val jettyTest = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "test"

  val mockito = "org.mockito" % "mockito-all" % "1.10.19" % "test"
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.3" % "test"

  val jodaDependencies = Seq(jodaTime, jodaConvert)
  val scalatraStack = Seq(scalatra, scalatraScalatest, scalatraJson, scalatraSwagger, json4s, scalatraAuth, commonsLang)

  val testingDependencies = Seq(mockito, scalatest)

  val javaxMail = "javax.mail" % "javax.mail-api" % "1.5.2"

  val seleniumVer = "2.44.0"
  val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVer % "test"
  val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVer % "test"
  val fest = "org.easytesting" % "fest-assert" % "1.4" % "test"
  val awaitility = "com.jayway.awaitility" % "awaitility-scala" % "1.6.3" % "test"
  val fakeMongo = "com.github.fakemongo" % "fongo" % "1.5.9" % "test"
  val mongoJava = "org.mongodb" % "mongo-java-driver" % "2.12.4"

  val selenium = Seq(seleniumJava, seleniumFirefox, fest)

  // If the scope is provided;test, as in scalatra examples then gen-idea generates the incorrect scope (test).
  // As provided implies test, so is enough here.
  val servletApiProvided = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided" artifacts (Artifact("javax.servlet", "jar", "jar"))

  val bson = "org.mongodb" % "bson" % "2.12.4" % "provided"

  val rogueField = "com.foursquare" %% "rogue-field" % rogueVersion intransitive()
  val rogueCore = "com.foursquare" %% "rogue-core" % rogueVersion intransitive()
  val rogueLift = "com.foursquare" %% "rogue-lift" % rogueVersion intransitive()
  val rogueIndex = "com.foursquare" %% "rogue-index" % rogueVersion intransitive()
  val liftMongoRecord = "net.liftweb" %% "lift-mongodb-record" % "2.6-M4" exclude("org.mongodb","mongo-java-driver")

  val rogue = Seq(rogueCore, rogueField, rogueLift, rogueIndex, liftMongoRecord)
}

object BootzookaBuild extends Build {

  import BuildSettings._
  import Dependencies._
  import com.earldouglas.xsbtwebplugin.WebPlugin.webSettings

  private def haltOnCmdResultError(result: Int) {
    if(result != 0) {
      throw new Exception("Build failed.")
    }
  }

  val updateNpm = baseDirectory map { bd =>
    println("Updating NPM dependencies")
    haltOnCmdResultError(Process("npm install", bd / ".." / "bootzooka-ui")!)
  }

  def gruntTask(taskName: String) = (baseDirectory, streams) map { (bd, s) =>
    val localGruntCommand = "./node_modules/.bin/grunt " + taskName
    def buildGrunt() = {
      Process(localGruntCommand, bd / ".." / "bootzooka-ui").!
    }
    println("Building with Grunt.js : " + taskName)
    haltOnCmdResultError(buildGrunt())
  } dependsOn updateNpm

  lazy val parent: Project = Project(
    "bootzooka-root",
    file("."),
    settings = buildSettings
  ) aggregate(backend, ui, dist)

  lazy val backend: Project = Project(
  "bootzooka-backend",
  file("bootzooka-backend"),
    settings = buildSettings ++ graphSettings ++ webSettings ++ Seq(libraryDependencies ++= (Seq(bson) ++ jodaDependencies
      ++ rogue ++ Seq(mongoJava, fakeMongo, jettyContainer)
          ++ Seq(commonsValidator, javaxMail, typesafeConfig, servletApiProvided)
          ++ scalatraStack))
    ++ Seq(
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
      packageWar in DefaultConf <<= (packageWar in DefaultConf) dependsOn gruntTask("build")
    ))

  lazy val ui = Project(
    "bootzooka-ui",
    file("bootzooka-ui"),
    settings = buildSettings ++ Seq(
      test in Test <<= (test in Test) dependsOn(gruntTask("test"))
    )
  )

  lazy val dist = Project(
    "bootzooka-dist",
    file("bootzooka-dist"),
    settings = buildSettings ++ assemblySettings ++ Seq(
      libraryDependencies ++= Seq(jetty),
      mainClass in assembly := Some("com.softwaremill.bootzooka.Bootzooka"),
      // We need to include the whole webapp, hence replacing the resource directory
      unmanagedResourceDirectories in Compile <<= baseDirectory { bd => {
        List(bd.getParentFile / backend.base.getName / "src" / "main", bd.getParentFile / ui.base.getName / "dist")
      } }
    )
  ) dependsOn (ui, backend)

  lazy val uiTests = Project(
    "bootzooka-ui-tests",
    file("bootzooka-ui-tests"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= selenium ++ Seq(awaitility, jettyTest, servletApiProvided)
    ) ++ Seq(
      test in Test <<= (test in Test) dependsOn (Keys.`package` in Compile in backend)
    )
  ) dependsOn (dist)
}
