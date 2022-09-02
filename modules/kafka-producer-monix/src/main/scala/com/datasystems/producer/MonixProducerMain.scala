package com.datasystems.producer

object MonixProducerMain {

  def main(args: Array[String]): Unit = {
//    println(s"Value of args: ${args.mkString("Array(", ", ", ")")}")
//    val conf = ConfigFactory.load()
//    val envProps = conf.getConfig(args(0))
//    val kafkaTopic = envProps.getString("topic")
    val producer = new MonixKafkaProducer()
    producer.start()
  }

}
