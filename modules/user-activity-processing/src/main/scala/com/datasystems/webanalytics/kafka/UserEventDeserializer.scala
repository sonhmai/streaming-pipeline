package com.datasystems.webanalytics.kafka

import java.nio.charset.StandardCharsets
import org.apache.flink.api.common.typeinfo.{TypeHint, TypeInformation}
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.ConsumerRecord
import io.circe.parser.decode
import com.datasystems.webanalytics.UserEvent

class UserEventDeserializer extends KafkaRecordDeserializationSchema[UserEvent] {

  private val log = org.log4s.getLogger

  import UserEvent.JsonImplicits.UserEventDecoder
  override def deserialize(
      record: ConsumerRecord[Array[Byte], Array[Byte]],
      out: Collector[UserEvent]
  ): Unit = {
    val jsonString = new String(record.value(), StandardCharsets.UTF_8)
    decode[UserEvent](jsonString) match {
      case Right(userEvent) => out.collect(userEvent)
      case Left(error) => log.error(s"Got error $error when parsing $jsonString from Kafka")
    }
  }

  override def getProducedType: TypeInformation[UserEvent] =
    TypeInformation.of(new TypeHint[UserEvent] {})
}
