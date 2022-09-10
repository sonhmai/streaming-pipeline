# streaming-pipeline
Building POC of streaming pipeline with Flink, Kafka, Pinot

TODO
- [x] Kafka + message producer with Kafka client
- [ ] (Optional) Kafka producer with Monix
- [x] Flink dummy forwarding app
- [ ] Flink deduplication app
- [ ] Flink app basic metrics and state monitoring (state size,..)
- [ ] Add Pinot to the pipeline
- [ ] Ingest Kafka to Pinot
- [ ] Kafka-Flink: Resiliency in case of failed serialization Kafka record
- [ ] Exactly once Flink + Kafka
- [ ] Exactly once Kafka + Pinot

## Quickstart

data generator -> input-user-events (Kafka) -> output-user-events (Kafka) -> Pinot

```bash
# in project root folder
docker-compose up

# create input topic
docker exec streaming-kafka-1 kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--topic input-user-events \
--if-not-exists

# create output topic
docker exec streaming-kafka-1 kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--topic output-user-events \
--if-not-exists

# produce 
sbt project kafkaProducer/run

# check producing to input correctly
docker exec --interactive --tty streaming-kafka-1 \
kafka-console-consumer --bootstrap-server localhost:9092 \
                       --topic input-user-events \
                       --from-beginning

# start flink app
sbt project userActivityProcessing/run

# consume from output topic
docker exec --interactive --tty streaming-kafka-1 \
kafka-console-consumer --bootstrap-server localhost:9092 \
                       --topic output-user-events \
                       --from-beginning

# ---- BELOW THIS LINE NOT YET IMPLEMENTED
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

## Details

> Which state to use for the DedupOperator?

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

> Kafka Connector Sink

- when to use what? KafkaRecordSerializationSchema, KafkaRecordSerializationSchemaBuilder, KafkaSerializerWrapper
    - `KafkaRecordSerializationSchemaBuilder` should be entry point when trying
    to serialize elements to `kafka.clients.Producer.ProducerRecord`. Access the builder
    via `KafkaRecordSerializationSchema.builder[T]()`
    - `KafkaSerializerWrapper` is not marked as public, so it should not be used.

> When to use RichFunction over Function?
- need to manage the function life cycle
- need to access runtime context

> Difference between RichFunction and ProcessFunction?
- ProcessFunction is in DataStream API `flink-streaming` org.apache.flink.streaming.api.functions
, while RichFunction is in common API `flink-core` org.apache.flink.api.common.functions
- ProcessFunction extends RichFunction -> can access setup, teardown, runtime
context API if RichFunction

> Can a state like `MapState` or `ValueState` be used in RichFilterFunction?
- yes if this is used on a KeyedStream, i.e. after a keyBy method



## Concepts
`Operator` is a source, a sink, or it applies an operation to one or more inputs,
producing a result.

## Common Issues
> If use scala dependencies, use Scala <= 2.12.8, [if not you have to build for yourself](https://nightlies.apache.org/flink/flink-docs-release-1.15/docs/dev/configuration/advanced/#scala-versions)

Scala versions after 2.12.8 are not binary compatible with previous 
2.12.x versions. This prevents the Flink project from upgrading its 
2.12.x builds beyond 2.12.8.
---

> When running Flink in IDE, `Caused by: java.lang.ClassNotFoundException`

This is probably because you do not have all required Flink dependencies 
implicitly loaded into the classpath.

IntelliJ IDEA: Go to Run > Edit Configurations > Modify options > 
Select include dependencies with "Provided" scope. 
This run configuration will now include all required classes to 
run the application from within the IDE
---

> Using IntelliJ IDEA, error Library does not match byte code when viewing 
> source of Kafka/ Flink

Probaby because of 2 library depends on 1 lib of different version -> fix, isolate
the dependency, remove the cached libs and reindex.

sbt uses coursier lib cache on MacOS `~/Library/Caches/Coursier/v1/https/`

## Command Cheatsheet

```bash
# produce to Kafka topic using console producer
docker exec --interactive --tty streaming-kafka-1 kafka-console-producer \
--bootstrap-server localhost:9092 \
--topic input-user-events

# consumer from the end (latest)          
docker exec --interactive --tty streaming-kafka-1 \
kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic input-user-events 
```

## References
- https://github.com/kbastani/pinot-wikipedia-event-stream
- https://github.com/tashoyan/telecom-streaming