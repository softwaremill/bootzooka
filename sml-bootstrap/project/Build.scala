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

  val buildSettings = Defaults.defaultSettings ++ Seq (
    resolvers     := bootstrapResolvers,
    scalacOptions += "-unchecked",
    classpathTypes ~= (_ + "orbit")
  )
}

object Dependencies {
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.6"
  val scalatra = "org.scalatra" % "scalatra" % "2.1.1"
  val scalate  = "org.scalatra" % "scalatra-scalate" % "2.1.1"
  val scalatraSpec2 = "org.scalatra" % "scalatra-specs2" % "2.1.1" % "test"
  val liftJson = "net.liftweb" %% "lift-json" % "2.5-M1"
  val liftJsonExt = "net.liftweb" %% "lift-json-ext" % "2.5-M1"

  val jettyOrbit = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))

  val scalatraStack = Seq(scalatra, scalate, scalatraSpec2, liftJson, liftJsonExt, logback, jettyOrbit)
}

object SmlBootstrapBuild extends Build {
  import Dependencies._
  import BuildSettings._

  lazy val parent: Project = Project(
    "boostrap-root",
    file("."),
    settings = buildSettings ++ Seq(libraryDependencies := scalatraStack)
  ) //aggregate(data, service, rest, ui)

  //  lazy val data: Project = Project(
  //    "bootstrap-data",
  //    file("boostrap-data"),
  //    settings = buildSettings
  //  )
  //
  //  lazy val service: Project = Project(
  //    "bootstrap-service",
  //    file("boostrap-service"),
  //    settings = buildSettings
  //  ) dependsOn(data)
  //
  //  lazy val rest: Project = Project(
  //    "bootstrap-rest",
  //    file("boostrap-rest"),
  //    settings = buildSettings
  //  ) dependsOn(data, service)
  //
  //  lazy val ui: Project = Project(
  //    "bootstrap-ui",
  //    file("boostrap-ui"),
  //    settings = buildSettings
  //  ) dependsOn(rest, data, service)

}
