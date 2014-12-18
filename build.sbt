import de.johoop.jacoco4sbt._
import JacocoPlugin._
import de.johoop.findbugs4sbt.FindBugs._
import de.johoop.findbugs4sbt.ReportType

organization := "org.asyncrpc"

name := "async-rpc"

version := "0.0.1"

scalaVersion := "2.10.4"

val jettyVersion = "9.2.4.v20141103"

libraryDependencies ++= Seq(
  "io.netty" % "netty-all" % "4.0.24.Final",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.mockito" % "mockito-all" % "1.10.8" % "test"
)

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

scalacOptions ++= Seq("-unchecked", "-deprecation")

jacoco.settings

seq(findbugsSettings : _*)

findbugsReportType := Some(ReportType.Html)
