import sbt._

object Dependencies {

  object Versions {
    lazy val flinkVersion = "1.15.1"
    lazy val log4sVersion = "1.10.0"

    // testing
    lazy val testContainerVersion = "1.17.3"
    lazy val scalaTestVersion = "3.2.12"
  }

  import Versions._

  private lazy val flinkScalaDependencies: Seq[ModuleID] = Seq(
    "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
    "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided"
  )

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

  lazy val userActivityDependencies: Seq[ModuleID] = flinkScalaDependencies ++
    flinkTest ++
    flinkKafka ++
    testDependencies ++
    logging

}
