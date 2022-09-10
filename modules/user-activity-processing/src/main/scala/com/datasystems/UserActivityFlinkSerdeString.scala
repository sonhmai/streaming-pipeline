package com.datasystems
import com.datasystems.webanalytics.config.UserActivityAppConfig
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment

/**
 * Simple app using String schema
 */
object UserActivityFlinkSerdeString {
  def run(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    val config = UserActivityAppConfig(
      kafkaBrokers = "localhost:9092",
      kafkaInputTopic = "input-user-events",
      kafkaInputConsumerGroupId = "user-events-group",
      kafkaOutputTopic = "output-user-events"
    )

    val kafkaSource: KafkaSource[String] = KafkaSource
      .builder[String]()
      .setBootstrapServers(config.kafkaBrokers)
      .setTopics(config.kafkaInputTopic)
      .setGroupId(config.kafkaInputConsumerGroupId)
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()

    val serializer: KafkaRecordSerializationSchema[String] =
      KafkaRecordSerializationSchema
        .builder[String]()
        .setTopic(config.kafkaOutputTopic)
        .setValueSerializationSchema(new SimpleStringSchema())
        .build()

    val kafkaSink: KafkaSink[String] = KafkaSink
      .builder[String]()
      .setDeliverGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
      .setBootstrapServers(config.kafkaBrokers)
      .setRecordSerializer(serializer)
      .build()

    env
      .fromSource[String](
        kafkaSource,
        WatermarkStrategy.noWatermarks(),
        "user-events"
      )
      .sinkTo(kafkaSink)

    env.execute(this.getClass.getSimpleName)
    ()
  }
}
