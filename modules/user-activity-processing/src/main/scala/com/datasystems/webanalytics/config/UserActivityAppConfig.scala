package com.datasystems.webanalytics.config

case class UserActivityAppConfig(
    kafkaBrokers: String,
    kafkaInputTopic: String,
    kafkaInputConsumerGroupId: String,
    kafkaOutputTopic: String
)
