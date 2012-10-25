name := "spray-mylog-demo"

version := "1.0-SNAPSHOT"

scalaVersion := "2.9.2"

scalacOptions := Seq("-deprecation", "-encoding", "utf8", "-Ydependent-method-types", "-unchecked")

resolvers += "spray.io" at "http://repo.spray.io"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "repo.codahale.com" at "http://repo.codahale.com"


libraryDependencies += "io.spray"                   %   "spray-servlet"     % "1.0-M4"

libraryDependencies += "io.spray"                   %   "spray-routing"     % "1.0-M4"

libraryDependencies += "io.spray"                   %%  "spray-json"        % "1.2.2"

libraryDependencies += "com.typesafe.akka"          %   "akka-actor"        % "2.0.3"

libraryDependencies += "com.typesafe.akka"          %   "akka-slf4j"        % "2.0.3"

libraryDependencies += "net.liftweb"                %%  "lift-util"         % "2.5-M1"

libraryDependencies += "org.eclipse.jetty"          %   "jetty-webapp"      % "8.1.7.v20120910" % "container"

libraryDependencies += "org.eclipse.jetty.orbit"    %   "javax.servlet"     % "3.0.0.v201112011016" artifacts Artifact("javax.servlet", "jar", "jar")

libraryDependencies += "io.spray"                   %% "twirl-api"          % "0.6.0"

libraryDependencies += "ch.qos.logback"             % "logback-classic"     % "0.9.26"

seq(Twirl.settings: _*)

seq(webSettings :_*)
