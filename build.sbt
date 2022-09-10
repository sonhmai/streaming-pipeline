import sbt._
import Dependencies._

ThisBuild / name := "streaming-pipeline"
ThisBuild / scalaVersion := "2.13.8"

ThisBuild / resolvers ++= Seq(
  "Confluent Maven Repo" at "https://packages.confluent.io/maven/",
  Resolver.mavenLocal
)

lazy val userActivity = (project in file("modules/user-activity"))
  .settings(libraryDependencies ++= userActivityDependencies)

lazy val userActivityProcessing = (project in file("modules/user-activity-processing"))
  .settings(libraryDependencies ++= userActivityProcessingDependencies)
  .dependsOn(userActivity)

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
    userActivityProcessing,
    kafkaProducer,
    kafkaProducerMonix
  )
