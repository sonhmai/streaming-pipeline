package com.datasystems.webanalytics
import java.util.UUID

trait UserEventGenerator {
  def generate(): UserEvent
}

class RandomUserEventGenerator extends UserEventGenerator {
  private val r = new scala.util.Random()

  override def generate(): UserEvent = {
    // random id from 1,000,000 to 2,000,000
    UserEvent(
      userID = (1000000 + r.nextInt(1000000)).toString,
      eventID = UUID.randomUUID(),
      userAction = Actions(r.nextInt(Actions.maxId)),
      eventTimestamp = System.currentTimeMillis()
    )
  }
}

