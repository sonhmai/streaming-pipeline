package com.datasystems.producer

import scala.concurrent.duration.DurationInt
import monix.eval.Task
import monix.execution.ExecutionModel.AlwaysAsyncExecution
import monix.execution.{Cancelable, Scheduler}
import monix.kafka.{KafkaProducer, KafkaProducerConfig, Serializer}
import org.apache.kafka.clients.producer.RecordMetadata
import com.datasystems.webanalytics.{RandomUserEventGenerator, UserEvent}
import monix.execution.schedulers.SchedulerService

case class ProducerAppConfig(
    kafkaTopic: String,
    kafkaBootstrapServers: List[String]
)

// TODO - not working, seems to be scheduler problem and need to have
//  separate scheduler
class MonixKafkaProducer {
  private implicit val scheduler: SchedulerService = Scheduler.singleThread(
    name = "kafka-producer",
    daemonic = false,
    executionModel = AlwaysAsyncExecution
  )
  private val userEventGenerator = new RandomUserEventGenerator()
  private val config = ProducerAppConfig(
    kafkaTopic = "quickstart",
    kafkaBootstrapServers = List("localhost:9092")
  )
  private implicit val serializer: Serializer[UserEvent] =
    KafkaJsonSerde.serializer(Map.empty[String, String], isKey = false)
  private var producer: KafkaProducer[String, UserEvent] = _

  def start(): Cancelable = {
    val producerConfig = KafkaProducerConfig.default.copy(
      bootstrapServers = config.kafkaBootstrapServers
    )
    producer = KafkaProducer[String, UserEvent](producerConfig, scheduler)

    scheduler.scheduleWithFixedDelay(
      2.seconds,
      1.seconds
    )(
      produceUserEventsToKafka(config, producer).runSyncUnsafe(timeout =
        5.seconds
      )
    )
  }

  def produceUserEventsToKafka(
      config: ProducerAppConfig,
      producer: KafkaProducer[String, UserEvent]
  ): Task[Option[RecordMetadata]] = {
    val userEvent = userEventGenerator.generate()
    println(userEvent)
    val recordMetadataTask = producer.send(
      config.kafkaTopic,
      key = userEvent.userID,
      value = userEvent
    )
    recordMetadataTask
  }

  def close(): Unit = {
    producer.close().runSyncUnsafe()
  }

}
