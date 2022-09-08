# streaming-pipeline
Building POC of streaming pipeline with Flink, Kafka, Pinot

- Type of event time in Flink app?

TODO
- [ ] Kafka + message producer
- [ ] Flink deduplication app
- [ ] Pinot
- [ ] Ingest Kafka to Pinot

```bash

# project root 
docker-compose up

# create topic
docker exec streaming-kafka-1 kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--topic quickstart

# produce 
docker exec --interactive --tty streaming-kafka-1 kafka-console-producer --bootstrap-server localhost:9092 --topic quickstart

# consume from beginning
docker exec --interactive --tty streaming-kafka-1 \
kafka-console-consumer --bootstrap-server localhost:9092 \
                       --topic quickstart \
                       --from-beginning
             
# consumer from the end (latest)          
docker exec --interactive --tty streaming-kafka-1 \
kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic quickstart 


# run pinot
docker run \
    --network=streaming-pipeline_default \
    -v /tmp/pinot-quick-start:/tmp/pinot-quick-start \
    apachepinot/pinot:latest AddTable \
    -schemaFile integration/web-user-events/pinot-table-schema-realtime-user-events.json \
    -tableConfigFile integration/web-user-events/pinot-table-config-realtime-user-events.json \
    -controllerHost manual-pinot-controller \
    -controllerPort 9000 \
    -exec
    
~/Softwares/apache-pinot-0.10.0-bin/bin/pinot-admin.sh QuickStart -type stream
# goto localhost:9000 to query the data

# how to create new user events realtime table? upload schema and table config?
# how to connect pinot and the kafka running in docker?
# can I create the table through the UI?
```

Running Kafka producer which continuously produces user events to Kafka
- Run class KafkaProducerMain in IDE
- sbt project kafkaProducer run

## Design

Dedup

Events -> DedupOperator -> Deduped-Events

- `Which state to use for the DedupOperator?` 

should use a ValueState<ConcurrentHashSet> or MapState<String, _>? 

Available state
  - KeyedState
    - ValueState
    - ListState
    - ReducingState
    - AggregatingState
    - MapState
  - OperatorState: should not be used because it is bounded to 1 parallel instance. One
  parallel instance does have all the state for dedup, e.g. one eventID "1" processed
  by 1 operator instance but after that another came to another instance, eventID "1"
  does not exist in that operator state, hence it is processed again, in case we have
  Kafka as a sink there are 2 events with eventID "1" in the sink topic.
  - BroadcastState: useful in case of sharing states btw streams, here there is only 1 stream.

## Scala
- Use Scala <= 2.12.8, [if not you have to build for yourself](https://nightlies.apache.org/flink/flink-docs-release-1.15/docs/dev/configuration/advanced/#scala-versions)
> Scala versions after 2.12.8 are not binary compatible with previous 
> 2.12.x versions. This prevents the Flink project from upgrading its 
> 2.12.x builds beyond 2.12.8.

## References
- https://github.com/kbastani/pinot-wikipedia-event-stream
- https://github.com/tashoyan/telecom-streaming