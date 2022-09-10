package com.datasystems.webanalytics.kafka

import com.datasystems.webanalytics.UserEvent
import org.apache.flink.api.common.serialization.SerializationSchema

import java.nio.charset.StandardCharsets

class UserEventSerializer extends SerializationSchema[UserEvent] {

  private val log = org.log4s.getLogger

  override def serialize(
      element: UserEvent
  ): Array[Byte] = {
    element.userID.getBytes(StandardCharsets.UTF_8)
  }

}
