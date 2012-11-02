organization := "pl.softwaremill"

name := "My Scalatra Web App"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.2"

seq(webSettings :_*)

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container"
)
