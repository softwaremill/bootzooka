import sbt._
import Keys._
import com.gu.SbtJasminePlugin._
import net.virtualvoid.sbt.graph.Plugin._

object Resolvers {

  val bootstrapResolvers = Seq(
    "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "SotwareMill Public Releases" at "http://tools.softwaremill.pl/nexus/content/repositories/releases/",
    "JBoss Releases" at "https://repository.jboss.org/nexus/content/groups/public",
    "Twitter Maven" at "http://maven.twttr.com",
    "Novus Releases" at "http://repo.novus.com/releases/"
  )

}

object BuildSettings {

  import Resolvers._

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "pl.softwaremill",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.9.2",

    resolvers := bootstrapResolvers,
    scalacOptions += "-unchecked",
    classpathTypes ~= (_ + "orbit"),
    libraryDependencies ++= Dependencies.testingDependencies,
    libraryDependencies ++= Dependencies.logging
  )

}

object Dependencies {

  val slf4jVersion = "1.7.2"
  val logBackVersion = "1.0.9"

  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
  val logBackClassic = "ch.qos.logback" % "logback-classic" % logBackVersion
  val jclOverSlf4j = "org.slf4j" % "jcl-over-slf4j" % slf4jVersion
  val slf4s = "com.weiglewilczek.slf4s" % "slf4s_2.9.1" % "1.0.7"

  val logging = Seq(slf4jApi, logBackClassic, jclOverSlf4j, slf4s)

  val casbah = "org.mongodb" %% "casbah" % "2.4.1"
  val salat = "com.novus" %% "salat" % "1.9.1"
  val databaseLibs = Seq(casbah, salat)

  val scalatraVersion = "2.2.0-RC1"

  val scalatra = "org.scalatra" % "scalatra" % scalatraVersion
  val scalatraSpec2 = "org.scalatra" % "scalatra-specs2" % scalatraVersion % "test"
  val scalatraJson = "org.scalatra" % "scalatra-json" % scalatraVersion
  val json4s = "org.json4s" %% "json4s-jackson" % "3.0.0"
  val scalatraAuth = "org.scalatra" % "scalatra-auth" % scalatraVersion  exclude("commons-logging", "commons-logging")

  val jodaTime = "joda-time" % "joda-time" % "2.0"
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"

  val commonsValidator = "commons-validator" % "commons-validator" % "1.4.0" exclude("commons-logging", "commons-logging")
  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.1"

  val jetty = "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container"

  val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"
  val specs2 = "org.specs2" %% "specs2" % "1.12.3" % "test"

  val jodaDependencies = Seq(jodaTime, jodaConvert)
  val scalatraStack = Seq(scalatra, scalatraSpec2, scalatraJson, json4s, scalatraAuth, commonsLang)

  val testingDependencies = Seq(mockito, specs2)

  // If the scope is provided;test, as in scalatra examples then gen-idea generates the incorrect scope (test).
  // As provided implies test, so is enough here.
  val servletApiProvided = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided" artifacts (Artifact("javax.servlet", "jar", "jar"))

}

object SmlBootstrapBuild extends Build {

  import Dependencies._
  import BuildSettings._
  import com.github.siasia.WebPlugin.webSettings

  lazy val parent: Project = Project(
    "bootstrap-root",
    file("."),
    settings = buildSettings
  ) aggregate(common, domain, dao, service, rest, ui)

  lazy val common: Project = Project(
    "bootstrap-common",
    file("bootstrap-common"),
    settings = buildSettings ++ Seq(libraryDependencies ++= jodaDependencies)
  )

  lazy val domain: Project = Project(
    "bootstrap-domain",
    file("bootstrap-domain"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(salat))
  ) dependsOn (common)

  lazy val dao: Project = Project(
    "bootstrap-dao",
    file("bootstrap-dao"),
    settings = buildSettings ++ Seq(libraryDependencies ++= databaseLibs)
  ) dependsOn(domain, common)

  lazy val service: Project = Project(
    "bootstrap-service",
    file("bootstrap-service"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(commonsValidator, casbah))
  ) dependsOn(domain, dao, common)

  lazy val rest: Project = Project(
    "bootstrap-rest",
    file("bootstrap-rest"),
    settings = buildSettings ++ Seq(libraryDependencies ++= scalatraStack ++ jodaDependencies ++ Seq(servletApiProvided))
  ) dependsOn(service, domain, common)

  lazy val ui: Project = Project(
    "bootstrap-ui",
    file("bootstrap-ui"),
    settings = buildSettings ++ jasmineSettings ++ graphSettings ++ webSettings ++ Seq(
      artifactName := { (config: ScalaVersion, module: ModuleID, artifact: Artifact) =>
        "bootstrap." + artifact.extension // produces nice war name -> http://stackoverflow.com/questions/8288859/how-do-you-remove-the-scala-version-postfix-from-artifacts-builtpublished-wi
      },
      libraryDependencies ++= Seq(jetty, servletApiProvided),
      appJsDir <+= sourceDirectory { src => src / "main" / "webapp" / "app" },
      appJsLibDir <+= sourceDirectory { src => src / "main" / "webapp" / "assets" / "js" },
      jasmineTestDir <+= sourceDirectory { src => src / "test" / "unit" },
      jasmineConfFile <+= sourceDirectory { src => src / "test" / "unit" / "test.dependencies.js" },
      jasmineRequireJsFile <+= sourceDirectory { src => src / "main" / "webapp" / "assets" / "js" / "require" / "require-2.0.6.js" },
      jasmineRequireConfFile <+= sourceDirectory { src => src / "test" / "unit" / "require.conf.js" },
      (test in Test) <<= (test in Test) dependsOn (jasmine))
  ) dependsOn (rest)

}
