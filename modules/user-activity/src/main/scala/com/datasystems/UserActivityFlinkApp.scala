package com.datasystems

import com.datasystems.webanalytics.UserEvent
import com.datasystems.webanalytics.config.UserActivityAppConfig
import com.datasystems.webanalytics.kafka.UserEventDeserializer
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaSink, KafkaSinkBuilder}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

object UserActivityFlinkApp {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    val config = UserActivityAppConfig(
      kafkaBrokers = "localhost:9092"
    )

    val kafkaSource: KafkaSource[UserEvent] = KafkaSource
      .builder()
      .setBootstrapServers(config.kafkaBrokers)
      .setTopics("input-user-events")
      .setGroupId("user-events-group")
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setDeserializer(new UserEventDeserializer())
      .build()

    // TODO - add deserialization into UserEvent message

    val kafkaSink: KafkaSink[UserEvent] = KafkaSink
      .builder()
      .setDeliverGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
      .setBootstrapServers(config.kafkaBrokers)
      .build()

    env
      .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "user-events")
      .sinkTo(kafkaSink)

    env.execute(this.getClass.getSimpleName)
    ()
  }
}
