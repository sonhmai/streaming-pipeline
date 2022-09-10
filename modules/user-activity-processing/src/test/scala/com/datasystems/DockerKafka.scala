package com.datasystems

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName


object DockerKafka {
  private val log = org.log4s.getLogger
  private val ImageName = "confluentinc/cp-kafka:6.2.1"
  private val KafkaContainer = new KafkaContainer(DockerImageName.parse(ImageName))

  def start(): Unit = {
    KafkaContainer.start()
  }

  def stop(): Unit = {
    KafkaContainer.stop()
  }

  def bootstrapServer: String = {
    val servers = KafkaContainer.getBootstrapServers
    log.info(s"Kafka bootstrap servers: $servers")
    servers
  }

}