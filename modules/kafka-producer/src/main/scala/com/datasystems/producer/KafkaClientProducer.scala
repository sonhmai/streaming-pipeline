package com.datasystems.producer

import java.util.Properties
import scala.io.Source
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.apache.kafka.clients.producer.{
  KafkaProducer,
  ProducerConfig,
  ProducerRecord
}
import com.datasystems.webanalytics.{RandomUserEventGenerator, UserEvent}
import io.circe.syntax.EncoderOps

class KafkaClientProducer(
    kafkaTopic: String
) {

  private val props = new Properties()
  props.put(
    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
    "localhost:9092"
  )
  props.put(
    ProducerConfig.CLIENT_ID_CONFIG,
    "Streaming Pipeline Kafka Producer"
  )
  props.put(
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
    "org.apache.kafka.common.serialization.StringSerializer"
  )
  props.put(
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
    "org.apache.kafka.common.serialization.StringSerializer"
  )

  private val producer = new KafkaProducer[String, String](props)
  private val userEventGenerator = new RandomUserEventGenerator()
  import UserEvent.JsonImplicits._

  def produceRandomlyForever(): Unit = {
    while (true) {
      val userEvent = userEventGenerator.generate()
      val record = new ProducerRecord[String, String](
        kafkaTopic,
        userEvent.userID,
        userEvent.asJson.toString()
      )
      producer.send(record)
      Thread.sleep(2000)
    }
  }

  def produceFromFile(filename: String) = {
    val source = Source.fromFile(filename)
    for (line <- source.getLines()) {
      println(line)
      val record = new ProducerRecord[String, String](kafkaTopic, "key", line)
      producer.send(record)
      Thread.sleep(2000)
    }
    source.close()
    producer.close()
  }
}
