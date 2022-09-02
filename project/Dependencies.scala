import sbt._

object Dependencies {

  object Versions {
    lazy val flinkVersion = "1.15.1"
    lazy val log4sVersion = "1.10.0"
    lazy val kafkaClientVersion = "3.2.1"
    lazy val typesafeConfigVersion = "1.4.2"
    lazy val circeVersion = "0.14.1"

    // testing
    lazy val testContainerVersion = "1.17.3"
    lazy val scalaTestVersion = "3.2.12"
  }

  import Versions._

  private lazy val flinkScalaDependencies: Seq[ModuleID] = Seq(
    "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided"
  )

  private lazy val circeDependencies = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)

  private lazy val flinkTest = Seq(
    "org.apache.flink" % "flink-test-utils" % flinkVersion % Test
  )

  private lazy val flinkKafka: Seq[ModuleID] = Seq(
    "org.apache.flink" % "flink-connector-kafka" % flinkVersion
  )

  private lazy val testDependencies = Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "org.testcontainers" % "testcontainers" % testContainerVersion % Test,
    "org.testcontainers" % "kafka" % testContainerVersion % Test
  )

  private lazy val logging = Seq(
    "org.log4s" %% "log4s" % log4sVersion
  )

  private lazy val kafkaDependencies = Seq (
    "org.apache.kafka" % "kafka-clients" % kafkaClientVersion
  )

  private lazy val typeSafeConfig = Seq (
    "com.typesafe" % "config" % typesafeConfigVersion
  )

  lazy val userActivityDependencies: Seq[ModuleID] = flinkScalaDependencies ++
    flinkTest ++
    flinkKafka ++
    circeDependencies ++
    testDependencies ++
    logging

  lazy val kafkaProducerDependencies: Seq[ModuleID] = kafkaDependencies ++
    typeSafeConfig ++
    circeDependencies ++
    testDependencies ++
    logging

  lazy val kafkaProducerMonixDependencies: Seq[ModuleID] = Seq(
    "io.monix" %% "monix-kafka-1x" % "1.0.0-RC6",
    "io.confluent" % "kafka-json-serializer" % "7.2.1",
  ) ++
    testDependencies ++
    logging

}
