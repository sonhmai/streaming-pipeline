import sbt._
import Dependencies._

ThisBuild / name := "streaming-pipeline"
ThisBuild / scalaVersion := "2.12.16"  // TODO - use Flink java lib to use scala 2.13

ThisBuild / resolvers ++= Seq(
  "Confluent Maven Repo" at "https://packages.confluent.io/maven/",
  Resolver.mavenLocal
)

lazy val userActivity = (project in file("modules/user-activity"))
  .settings(libraryDependencies ++= userActivityDependencies)

lazy val kafkaProducer = (project in file("modules/kafka-producer"))
  .settings(libraryDependencies ++= kafkaProducerDependencies)
  .dependsOn(userActivity)

lazy val kafkaProducerMonix = (project in file("modules/kafka-producer-monix"))
  .settings(libraryDependencies ++= kafkaProducerMonixDependencies)
  .dependsOn(userActivity)

lazy val root = (project in file("."))
  .settings(name := (ThisBuild / name).value)
  .aggregate(
    userActivity,
    kafkaProducer,
    kafkaProducerMonix
  )
