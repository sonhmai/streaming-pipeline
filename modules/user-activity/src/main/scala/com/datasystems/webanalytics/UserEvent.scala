package com.datasystems.webanalytics

import com.datasystems.webanalytics.Actions.UserAction
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

import java.util.UUID

case class UserEvent(
    userID: String,
    eventID: UUID,
    userAction: UserAction,
    eventTimestamp: Long
)

object Actions extends Enumeration {
  type UserAction = Value
  val Click = Value("click")
  val View = Value("view")
}

object UserEvent {
  object JsonImplicits {
    lazy implicit val UserActionEncoder: Encoder[UserAction] =
      Encoder.encodeEnumeration(Actions)
    lazy implicit val UserEventEncoder: Encoder[UserEvent] =
      deriveEncoder[UserEvent]
  }
}
