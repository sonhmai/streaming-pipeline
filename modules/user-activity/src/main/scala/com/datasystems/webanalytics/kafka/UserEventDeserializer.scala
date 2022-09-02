package com.datasystems.webanalytics.kafka

import com.datasystems.webanalytics.UserEvent
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.ConsumerRecord

class UserEventDeserializer extends KafkaRecordDeserializationSchema[UserEvent] {
  override def deserialize(
      record: ConsumerRecord[Array[Byte], Array[Byte]],
      out: Collector[UserEvent]
  ): Unit = ???

  override def getProducedType: TypeInformation[UserEvent] = ???
}
