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
    scalaVersion := "2.9.1",

    resolvers := bootstrapResolvers,
    scalacOptions += "-unchecked",
    classpathTypes ~= (_ + "orbit")
  )
}

object Dependencies {
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.6"
  val scalatra = "org.scalatra" % "scalatra" % "2.1.1"
  val scalate = "org.scalatra" % "scalatra-scalate" % "2.1.1"
  val scalatraSpec2 = "org.scalatra" % "scalatra-specs2" % "2.1.1" % "test"
  val liftJson = "net.liftweb" %% "lift-json" % "2.5-M1"
  val liftJsonExt = "net.liftweb" %% "lift-json-ext" % "2.5-M1"
  val scalatraStack = Seq(scalatra, scalate, scalatraSpec2, liftJson, liftJsonExt, logback)

  val jettyOrbit = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
}

object SmlBootstrapBuild extends Build {

  import Dependencies._
  import BuildSettings._

  lazy val parent: Project = Project(
    "bootstrap-root",
    file("."),
    settings = buildSettings
  ) aggregate(domain, dao, service, rest, ui)

  lazy val domain: Project = Project(
    "bootstrap-domain",
    file("bootstrap-domain"),
    settings = buildSettings
  )

  lazy val dao: Project = Project(
    "bootstrap-dao",
    file("bootstrap-dao"),
    settings = buildSettings
  ) dependsOn (domain)

  lazy val service: Project = Project(
    "bootstrap-service",
    file("bootstrap-service"),
    settings = buildSettings
  ) dependsOn (domain, dao)

  lazy val rest: Project = Project(
    "bootstrap-rest",
    file("bootstrap-rest"),
    settings = buildSettings ++ Seq(libraryDependencies := scalatraStack ++ Seq(jettyOrbit))
  ) dependsOn (service, domain)

  lazy val ui: Project = Project(
    "bootstrap-ui",
    file("bootstrap-ui"),
    settings = buildSettings ++ Seq(libraryDependencies := Seq(jettyOrbit))
  ) dependsOn (rest)

}
