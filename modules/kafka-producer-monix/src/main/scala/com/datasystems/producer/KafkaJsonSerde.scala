package com.datasystems.producer

import collection.JavaConverters._

import io.confluent.kafka.serializers.KafkaJsonSerializer
import monix.kafka.{Serializer => MonixSerializer}

// https://www.madewithtea.com/posts/using-monix-with-kafka-avro-and-schema-registry
object KafkaJsonSerde {
  def serializer[T](config: Map[String, String], isKey: Boolean): MonixSerializer[T] =
    MonixSerializer[T](
      className = "io.confluent.kafka.serializers.KafkaJsonSerializer",
      classType = classOf[KafkaJsonSerializer[T]],
      constructor = _ => {
        val serializer = new KafkaJsonSerializer[T]()
        serializer.configure(config.asJava, isKey: Boolean)
        serializer
      }
    )
}
