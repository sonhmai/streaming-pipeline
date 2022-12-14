package com.datasystems

import com.datasystems.webanalytics.UserEvent
import com.datasystems.webanalytics.config.UserActivityAppConfig
import com.datasystems.webanalytics.kafka.{
  UserEventDeserializer,
  UserEventSerializer
}
import com.datasystems.webanalytics.operator.DedupRichFilter
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.java.functions.KeySelector
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{
  KafkaRecordSerializationSchema,
  KafkaSink
}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

object UserActivityFlinkApp {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    val config = UserActivityAppConfig(
      kafkaBrokers = "localhost:9092",
      kafkaInputTopic = "input-user-events",
      kafkaInputConsumerGroupId = "user-events-group",
      kafkaOutputTopic = "output-user-events"
    )

    val kafkaSource: KafkaSource[UserEvent] = KafkaSource
      .builder[UserEvent]()
      .setBootstrapServers(config.kafkaBrokers)
      .setTopics(config.kafkaInputTopic)
      .setGroupId(config.kafkaInputConsumerGroupId)
      .setStartingOffsets(OffsetsInitializer.latest())
      .setDeserializer(new UserEventDeserializer())
      .build()

    val serializer: KafkaRecordSerializationSchema[UserEvent] =
      KafkaRecordSerializationSchema
        .builder[UserEvent]()
        .setTopic(config.kafkaOutputTopic)
        .setValueSerializationSchema(new UserEventSerializer())
        .build()

    val kafkaSink: KafkaSink[UserEvent] = KafkaSink
      .builder[UserEvent]()
      .setDeliverGuarantee(DeliveryGuarantee.NONE)
      .setBootstrapServers(config.kafkaBrokers)
      .setRecordSerializer(serializer)
      .build()

    val userEvents: DataStream[UserEvent] = env
      .fromSource[UserEvent](
        kafkaSource,
        WatermarkStrategy.noWatermarks(),
        "user-events"
      )

    val keySelector = new KeySelector[UserEvent, String] {
      override def getKey(value: UserEvent): String = value.userID
    }

    userEvents
      .keyBy(keySelector)
      .filter(new DedupRichFilter)
      .sinkTo(kafkaSink)

    env.execute(this.getClass.getSimpleName)
    ()
  }
}
