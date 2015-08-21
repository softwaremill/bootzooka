addSbtPlugin("com.gu" % "sbt-teamcity-test-reporting-plugin" % "1.5")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.4.0")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.4.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

// for DeployToHeroku task
libraryDependencies += "com.heroku.sdk" % "heroku-deploy" % "0.4.3"
