package com.datasystems.producer

object KafkaProducerMain {
  def main(args: Array[String]): Unit = {
//    println(s"Value of args: ${args.mkString("Array(", ", ", ")")}")
//    val conf = ConfigFactory.load()
//    val envProps = conf.getConfig(args(0))
//    val filename = envProps.getString("input.json.base.dir")
//    val kafkaTopic = envProps.getString("topic")

    val producer = new KafkaClientProducer(kafkaTopic = "quickstart")
    producer.produceRandomlyForever()

  }
}
