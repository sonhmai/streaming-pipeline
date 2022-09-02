package com.datasystems

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.scala._

import com.datasystems.webanalytics.UserEvent
import com.datasystems.webanalytics.kafka.UserEventDeserializer

object UserActivityFlinkApp {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val brokers = "localhost:9092"

    val kafkaSource = KafkaSource
      .builder[UserEvent]()
      .setBootstrapServers(brokers)
      .setTopics("input-user-events")
      .setGroupId("user-events-group")
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setDeserializer(new UserEventDeserializer())
      .build()

    // TODO - add deserialization into UserEvent message

    val userEvents: DataStream[UserEvent] = env
      .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "user-events")

  }
}