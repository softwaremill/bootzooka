import sbt._
import Keys._
import net.virtualvoid.sbt.graph.Plugin._
import com.typesafe.sbt.SbtScalariform._
import com.earldouglas.xsbtwebplugin.PluginKeys._

object Resolvers {
  val bootzookaResolvers = Seq(
    "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "SotwareMill Public Releases" at "https://nexus.softwaremill.com/content/repositories/releases/",
    "SotwareMill Public Snapshots" at "https://nexus.softwaremill.com/content/repositories/snapshots/",
    "TorqueBox Releases" at "http://rubygems-proxy.torquebox.org/releases"
  )
}

object BuildSettings {

  import Resolvers._

  val mongoDirectory = SettingKey[File]("mongo-directory", "The home directory of MongoDB datastore")

  val buildSettings = Defaults.defaultSettings ++ Seq(mongoDirectory := file("")) ++ defaultScalariformSettings ++ Seq(

    organization := "com.softwaremill",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.10.0",

    resolvers := bootzookaResolvers,
    scalacOptions += "-unchecked",
    classpathTypes ~= (_ + "orbit"),
    libraryDependencies ++= Dependencies.testingDependencies,
    libraryDependencies ++= Dependencies.logging,
    libraryDependencies ++= Seq(Dependencies.guava, Dependencies.googleJsr305),

    parallelExecution := false, // We are starting mongo in tests.

    testOptions in Test <+= mongoDirectory map {
      md: File => Tests.Setup {
        () =>
          val mongoFile = new File(md.getAbsolutePath + "/bin/mongod")
          val mongoFileWin = new File(mongoFile.getAbsolutePath + ".exe")
          if (mongoFile.exists || mongoFileWin.exists) {
            System.setProperty("mongo.directory", md.getAbsolutePath)
          } else {
            throw new RuntimeException(
              "Trying to launch with MongoDB but unable to find it in 'mongo.directory' (%s). Please check your ~/.sbt/local.sbt file.".format(mongoFile.getAbsolutePath))
          }
      }
    }
  )

}

object Dependencies {

  val slf4jVersion = "1.7.2"
  val logBackVersion = "1.0.9"
  val smlCommonVersion = "75"
  val scalatraVersion = "2.2.1"
  val rogueVersion = "2.0.0-RC1"
  val scalaLoggingVersion = "1.0.1"

  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
  val logBackClassic = "ch.qos.logback" % "logback-classic" % logBackVersion
//  val jclOverSlf4j = "org.slf4j" % "jcl-over-slf4j" % slf4jVersion
  val scalaLogging = "com.typesafe" %% "scalalogging-slf4j" % scalaLoggingVersion

  val logging = Seq(slf4jApi, logBackClassic, scalaLogging)

  val guava = "com.google.guava" % "guava" % "13.0.1"
  val googleJsr305 = "com.google.code.findbugs" % "jsr305" % "2.0.1"

  val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
  val scalatraScalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
  val scalatraJson = "org.scalatra" %% "scalatra-json" % scalatraVersion
  val json4s = "org.json4s" %% "json4s-jackson" % "3.1.0"
  val scalatraAuth = "org.scalatra" %% "scalatra-auth" % scalatraVersion  exclude("commons-logging", "commons-logging")

  val jodaTime = "joda-time" % "joda-time" % "2.0"
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"

  val swaggerCore = "com.wordnik" % "swagger-core_2.10.0" % "1.2.0"
  val scalatraSwagger = "org.scalatra" %% "scalatra-swagger" % scalatraVersion

  val commonsValidator = "commons-validator" % "commons-validator" % "1.4.0" exclude("commons-logging", "commons-logging")
  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.1"

  val jetty = "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container"
  val jettyTest = "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "test"

  val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"
  val scalatest = "org.scalatest" %% "scalatest" % "1.9.1" % "test"

  val jodaDependencies = Seq(jodaTime, jodaConvert)
  val scalatraStack = Seq(scalatra, scalatraScalatest, scalatraJson, json4s, scalatraAuth, commonsLang, swaggerCore, scalatraSwagger)

  val testingDependencies = Seq(mockito, scalatest)

  val javaxMail = "javax.mail" % "mail" % "1.4.5"

  val smlCommonUtil = "com.softwaremill.common" % "softwaremill-util" % smlCommonVersion
  val smlCommonSqs = "com.softwaremill.common" % "softwaremill-sqs" % smlCommonVersion
  val smlCommonConfig = "com.softwaremill.common" % "softwaremill-conf" % smlCommonVersion

  val scalate = "org.fusesource.scalate" %% "scalate-core" % "1.6.0"

  val seleniumVer = "2.32.0"
  val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVer % "test"
  val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVer % "test"
  val fest = "org.easytesting" % "fest-assert" % "1.4" % "test"
  val awaitility = "com.jayway.awaitility" % "awaitility-scala" % "1.3.5" % "test"

  val selenium = Seq(seleniumJava, seleniumFirefox, fest)

  // If the scope is provided;test, as in scalatra examples then gen-idea generates the incorrect scope (test).
  // As provided implies test, so is enough here.
  val servletApiProvided = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided" artifacts (Artifact("javax.servlet", "jar", "jar"))

  val bson = "com.mongodb" % "bson" % "2.7.1" % "provided"

  val rogueField = "com.foursquare" %% "rogue-field" % rogueVersion intransitive()
  val rogueCore = "com.foursquare" %% "rogue-core" % rogueVersion intransitive()
  val rogueLift = "com.foursquare" %% "rogue-lift" % rogueVersion intransitive()
  val liftMongoRecord = "net.liftweb" %% "lift-mongodb-record" % "2.5-M4"

  val rogue = Seq(rogueCore, rogueField, rogueLift, liftMongoRecord)
}

object BootzookaBuild extends Build {

  import Dependencies._
  import BuildSettings._
  import com.github.siasia.WebPlugin.webSettings

  private def haltOnCmdResultError(result: Int) {
    if(result != 0) {
      throw new Exception("Build failed.")
    }
  }

  def updateNpm() = (baseDirectory, streams) map { (bd, s) =>
    println("Updating NPM dependencies")
    haltOnCmdResultError(Process("npm install", bd)!)
  }

  def gruntTask(taskName: String) = (baseDirectory, streams) map { (bd, s) =>
    val localGruntCommand = "./node_modules/.bin/grunt " + taskName
    def buildGrunt() = {
      Process(localGruntCommand, bd / ".." / "bootzooka-ui").!
    }
    println("Building with Grunt.js : " + taskName)
    haltOnCmdResultError(buildGrunt())
  }

  lazy val parent: Project = Project(
    "bootzooka-root",
    file("."),
    settings = buildSettings
  ) aggregate(common, domain, dao, service, rest, ui)

  lazy val common: Project = Project(
    "bootzooka-common",
    file("bootzooka-common"),
    settings = buildSettings ++ Seq(libraryDependencies ++= jodaDependencies)
  )

  lazy val domain: Project = Project(
    "bootzooka-domain",
    file("bootzooka-domain"),
    settings = buildSettings ++ Seq(libraryDependencies += bson)
  ) dependsOn (common)

  lazy val dao: Project = Project(
    "bootzooka-dao",
    file("bootzooka-dao"),
    settings = buildSettings ++ Seq(libraryDependencies ++= rogue ++ Seq(smlCommonUtil))
  ) dependsOn(domain, common)

  lazy val service: Project = Project(
    "bootzooka-service",
    file("bootzooka-service"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(commonsValidator, smlCommonSqs, smlCommonConfig,
      javaxMail, scalate))
  ) dependsOn(domain, dao, common)

  lazy val rest: Project = Project(
    "bootzooka-rest",
    file("bootzooka-rest"),
    settings = buildSettings ++ graphSettings ++ webSettings ++ Seq(
      libraryDependencies ++= scalatraStack ++ jodaDependencies ++ Seq(servletApiProvided, smlCommonConfig),
      artifactName := { (config: ScalaVersion, module: ModuleID, artifact: Artifact) =>
        "bootzooka." + artifact.extension // produces nice war name -> http://stackoverflow.com/questions/8288859/how-do-you-remove-the-scala-version-postfix-from-artifacts-builtpublished-wi
      },
      // We need to include the whole webapp, hence replacing the resource directory
      webappResources in Compile <<= baseDirectory {
        bd => {
          List(bd.getParentFile() / rest.base.getName / "src" / "main" / "webapp", bd.getParentFile() / ui.base.getName / "dist" / "webapp")
        }
      },
      packageWar in DefaultConf <<= (packageWar in DefaultConf) dependsOn gruntTask("build"),
      libraryDependencies ++= Seq(jetty, servletApiProvided)
    )
  ) dependsOn(service, domain, common)

  lazy val ui = Project(
    "bootzooka-ui",
    file("bootzooka-ui"),
    settings = buildSettings ++ Seq(
      test in Test <<= (test in Test) dependsOn(updateNpm(), gruntTask("test"))
    )
  )

  lazy val uiTests = Project(
    "bootzooka-ui-tests",
    file("bootzooka-ui-tests"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= selenium ++ Seq(awaitility, jettyTest, servletApiProvided)
    )

  ) dependsOn (rest)

}
