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


```

Running Kafka producer which continuously produces user events to Kafka
- Run class KafkaProducerMain in IDE
- sbt project kafkaProducer run