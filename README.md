# streaming-pipeline
Building POC of streaming pipeline with Flink, Kafka, Pinot

TODO
- [x] Kafka + message producer with Kafka client
- [ ] (Optional) Kafka producer with Monix
- [x] Flink dummy forwarding app
- [ ] Flink deduplication app
- [ ] Add Pinot to the pipeline
- [ ] Ingest Kafka to Pinot
- [ ] Deduplication state size estimate
- [ ] Flink app basic metrics and state monitoring (state size,..)
- [ ] Kafka-Flink: Resiliency in case of failed serialization Kafka record
- [ ] Exactly once Flink + Kafka
- [ ] Exactly once Kafka + Pinot
- [ ] Running end2end in CI pipeline

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
                       
# test kafka connection inside docker network
docker run --network=streaming-pipeline_default \
confluentinc/cp-kafkacat kafkacat -b kafka:9092 -L

# go to localhost:9000 to access pinot web UI
# docker run -v <local_dir>:<container_dir> # bind a local dir to volume of container
# add table, guide https://docs.pinot.apache.org/basics/components/schema#creating-a-schema
docker run \
    --network=streaming-pipeline_default \
    -v $(pwd)/integration:/tmp/integration \
    apachepinot/pinot:latest AddTable \
    -schemaFile /tmp/integration/web-user-events/user-events_schema.json \
    -tableConfigFile /tmp/integration/web-user-events/user-events_realtimeTableConfig.json \
    -controllerHost pinot-controller \
    -controllerPort 9000 \
    -exec

# or using the admin script
~/Softwares/apache-pinot-0.10.0-bin/bin/pinot-admin.sh AddSchema \
-schemaFile="./integration/web-user-events/pinot-table-schema-realtime-user-events.json" \
-exec
# how to create new user events realtime table? upload schema and table config?
# how to connect pinot and the kafka running in docker?
# can I create the table through the UI?

# go to Pinot UI localhost:9000 and query with this 
select * from "user-events" limit 10

# clean up stopped containers
docker-compose rm
```

Running Kafka producer which continuously produces user events to Kafka
- Run class KafkaProducerMain in IDE
- sbt project kafkaProducer run

## Design

Dedup
- State size esimate
  - 10M users (keyed by userID) ->??
  
- Reducing state size
  - State is partitioned by userID and is maintained for 1 day, eventID that
  was inserted for more than 1 day got deleted. The state per userID cannot 
  be simply dropped after 1 day though because it contains recent events.
  You can imagine that we want to keep the state of all eventID of a user
  as a sliding window of 1-day interval and it slides every 30 minutes.

Exactly-once Kafka-Flink-Kafka
- TODO

Exactly-once Kafka-Pinot
- TODO

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

> Difference between KeyedState and OperatorState?
1. `CurrentKey`: yes in KeyedState, no in OperatorState
2. `On-heap/Off-heap store`: only KS has option storing on RocksDB. OS always 
stored on-heap, KS backends support both on-heap and off-heap state objects.
3. `Snapshoting and Recovery`: OS manually. KS implemented automatically by Flink.
4. `State size`: frequently OS size < KS size

## Concepts
`Operator` is a source, a sink, or it applies an operation to one or more inputs,
producing a result.

`KeyedCoProcessFunction`
A KeyedCoProcessFunction connects two streams that have been keyed in 
compatible ways -- the two streams are mapped to the same keyspace 
-- making it possible for the KeyedCoProcessFunction to have keyed state 
that relates to both streams. 

For example, you might want to join a stream of customer transactions 
with a stream of customer updates -- joining them on the customer_id. 
You would implement this in Flink (if doing so at a low level) by keying 
both streams by the customer_id, and connecting those keyed streams 
with a KeyedCoProcessFunction.

## Common Issues
> If use scala dependencies, use Scala <= 2.12.8, [if not you have to build for yourself](https://nightlies.apache.org/flink/flink-docs-release-1.15/docs/dev/configuration/advanced/#scala-versions)

Scala versions after 2.12.8 are not binary compatible with previous 
2.12.x versions. This prevents the Flink project from upgrading its 
2.12.x builds beyond 2.12.8.

> When running Flink in IDE, `Caused by: java.lang.ClassNotFoundException`

This is probably because you do not have all required Flink dependencies 
implicitly loaded into the classpath.

IntelliJ IDEA: Go to Run > Edit Configurations > Modify options > 
Select include dependencies with "Provided" scope. 
This run configuration will now include all required classes to 
run the application from within the IDE

> Using IntelliJ IDEA, error Library does not match byte code when viewing 
> source of Kafka/ Flink

Probaby because of 2 library depends on 1 lib of different version -> fix, isolate
the dependency, remove the cached libs and reindex.

sbt uses coursier lib cache on MacOS `~/Library/Caches/Coursier/v1/https/`

> Pinot 
> java.lang.IllegalStateException: There are less instances: 
> 1 in instance partitions: user-events_CONSUMING than the table replication: 2

replicasPerPartition value in `segmentsConfig`  in realtimeTableConfig must be the same as num topic partition#

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

~/Softwares/apache-pinot-0.10.0-bin/bin/pinot-admin.sh QuickStart -type stream
# goto localhost:9000 to query the data

```

## References

Repos
- https://github.com/kbastani/pinot-wikipedia-event-stream
- https://github.com/tashoyan/telecom-streaming
- https://github.com/apache/flink-training

### Flink
State
- [Best Practices and Tips for Working with Flink State](https://www.alibabacloud.com/blog/best-practices-and-tips-for-working-with-flink-state-flink-advanced-tutorials_596630)
- [Manage state with huge memory usage](https://stackoverflow.com/questions/60040596/manage-state-with-huge-memory-usage-querying-from-storage)
- [Flink checkpoint interval and state size](https://stackoverflow.com/questions/55959692/flink-checkpoints-interval-and-state-size)
- [Flink checkpoint time and size increase](https://stackoverflow.com/questions/64309126/flink-checkpoints-size-are-growing-over-20gb-and-checkpoints-time-take-over-1-mi)

Join
- [Stream Joins for Large Time Windows with Flink](https://stackoverflow.com/questions/58178404/stream-joins-for-large-time-windows-with-flink)

Monitoring
- [Monitoring Large-Scale Apache Flink Applications, Part 1: Concepts & Continuous Monitoring](https://www.ververica.com/blog/monitoring-large-scale-apache-flink-applications-part-1-concepts-continuous-monitoring)

### Pinot
