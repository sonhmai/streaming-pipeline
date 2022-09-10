package com.datasystems.webanalytics.operator

import com.datasystems.webanalytics.UserEvent
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}

/**
 *  Dedup events using eventID, keyed by UserEvent.userID
 *
 *  Notes
 *  - is there a KeyedRichFilterFunction version in order to partition the state by
 *  key? yes, use `KeyedProcessFunction` in DataStream API.
 *
 *  TODO
 *    - how to clean state periodically?
 *    - is this state OperatorState or KeyedState? how to scale & make sure that it can
 *    works globally on the whole stream but not locally on a stream partition?
 *
 *
  */
class DedupRichFilter extends RichFilterFunction[UserEvent] {

  private lazy val state: MapState[String, String] = {
    val stateDescriptor = new MapStateDescriptor[String, String](
      "map",
      classOf[String],
      classOf[String]
    )
    getRuntimeContext.getMapState(stateDescriptor)
  }

  override def filter(value: UserEvent): Boolean = {
    val key = value.eventID.toString
    if (state.contains(key)) {
      false
    } else {
      state.put(key, key)
      true
    }
  }
}
