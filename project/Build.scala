import sbt._
import Keys._

object Resolvers {

  val bootstrapResolvers = Seq(
    "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "SotwareMill Public Releases" at "http://tools.softwaremill.pl/nexus/content/repositories/releases/",
    "JBoss Releases" at "https://repository.jboss.org/nexus/content/groups/public",
    "Twitter Maven" at "http://maven.twttr.com")

}

object BuildSettings {

  import Resolvers._

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "pl.softwaremill",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.9.2",

    resolvers := bootstrapResolvers,
    scalacOptions += "-unchecked",
    classpathTypes ~= (_ + "orbit")
  )

}

object Dependencies {

  val scalatraVersion = "2.2.0-RC1"

  val logback = "ch.qos.logback" % "logback-classic" % "1.0.6"
  val scalatra = "org.scalatra" % "scalatra" % scalatraVersion
  val scalatraSpec2 = "org.scalatra" % "scalatra-specs2" % scalatraVersion % "test"
  val scalatraJson = "org.scalatra" % "scalatra-json" % scalatraVersion
  val json4s = "org.json4s" %% "json4s-jackson" % "3.0.0"
  val scalatraAuth = "org.scalatra" % "scalatra-auth" % scalatraVersion
  val jodaTime = "joda-time" % "joda-time" % "2.0"
  val specs2 = "org.specs2" %% "specs2" % "1.12.3" % "test"
  val scalatraStack = Seq(scalatra, scalatraSpec2, scalatraJson, json4s, logback, scalatraAuth, jodaTime, specs2)

  val jettyOrbit = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))

}

object SmlBootstrapBuild extends Build {

  import Dependencies._
  import BuildSettings._

  lazy val parent: Project = Project(
    "bootstrap-root",
    file("."),
    settings = buildSettings
  ) aggregate(common, domain, dao, service, rest, ui)

  lazy val common: Project = Project(
    "bootstrap-common",
    file("bootstrap-common"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(specs2))
  )

  lazy val domain: Project = Project(
    "bootstrap-domain",
    file("bootstrap-domain"),
    settings = buildSettings
  ) dependsOn (common)

  lazy val dao: Project = Project(
    "bootstrap-dao",
    file("bootstrap-dao"),
    settings = buildSettings
  ) dependsOn(domain, common)

  lazy val service: Project = Project(
    "bootstrap-service",
    file("bootstrap-service"),
    settings = buildSettings
  ) dependsOn(domain, dao, common)

  lazy val rest: Project = Project(
    "bootstrap-rest",
    file("bootstrap-rest"),
    settings = buildSettings ++ Seq(libraryDependencies := scalatraStack ++ Seq(jettyOrbit))
  ) dependsOn(service, domain, common)

  lazy val ui: Project = Project(
    "bootstrap-ui",
    file("bootstrap-ui"),
    settings = buildSettings ++ Seq(libraryDependencies := Seq(jettyOrbit))
  ) dependsOn (rest)

}
