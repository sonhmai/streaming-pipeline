package com.datasystems.webanalytics.operator
import com.datasystems.webanalytics.UserEvent
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector

/** Dedup events using eventID, keyed by UserEvent.userID
  */
class DedupKeyedProcess extends KeyedProcessFunction[String, UserEvent, UserEvent] {
  override def processElement(
      value: UserEvent,
      ctx: KeyedProcessFunction[String, UserEvent, UserEvent]#Context,
      out: Collector[UserEvent]
  ): Unit = ???
}
