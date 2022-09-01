package com.datasystems.model
import java.sql.Timestamp
import java.util.UUID

case class UserEvent(
    userID: String,
    eventID: UUID,
    userAction: String,
    eventTimestamp: Timestamp
)
