import sbt._
import Dependencies._

ThisBuild / name := "streaming-pipeline"
ThisBuild / scalaVersion := "2.12.16"  // TODO - use Flink java lib to use scala 2.13

lazy val userActivity = (project in file("modules/user-activity"))
  .settings(libraryDependencies ++= userActivityDependencies)

lazy val root = (project in file("."))
  .settings(name := (ThisBuild / name).value)
  .aggregate(userActivity)
