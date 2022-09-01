
export NETWORK="streaming-pipeline"
export SCHEMA_FILE="/integration/.json"
export TABLE_CONFIG_FILE="/integration/.json"

# start kafka
docker run \
  --network $NETWORK --name=kafka \
  -e KAFKA_ZOOKEEPER_CONNECT=manual-zookeeper:2181/kafka \
  -e KAFKA_BROKER_ID=0 \
  -e KAFKA_ADVERTISED_HOST_NAME=kafka \
  -d wurstmeister/kafka:latest

# create kafka topic
docker exec \
  -t kafka \
  /opt/kafka/bin/kafka-topics.sh \
  --zookeeper manual-zookeeper:2181/kafka \
  --partitions=1 --replication-factor=1 \
  --create --topic topic-user-events

# upload schema and table config
docker run \
  --network $NETWORK \
  -v \
  --name pinot-streaming-table-creation \
  apachepinot/pinot:latest AddTable \
  -schemaFile $SCHEMA_FILE \
  -tableConfigFile $TABLE_CONFIG_FILE \
  -controllerHost manual-pinot-controller \
  -controllerHost 9000 \
  -exec

