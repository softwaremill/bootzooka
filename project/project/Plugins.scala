import sbt._

object Plugins extends Build {

  lazy val plugins = Project("plugins", file(".")).dependsOn(
    // TeamCity reporting, see: https://github.com/guardian/sbt-teamcity-test-reporting-plugin
    uri("git://github.com/guardian/sbt-teamcity-test-reporting-plugin.git#1.2"),
    // Shell access from sbt console, see https://github.com/steppenwells/sbt-sh
    uri("git://github.com/steppenwells/sbt-sh.git")
  )

}
