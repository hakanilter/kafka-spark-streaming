#!/bin/bash

if [ -z "$KAFKA_HOME" ]
  then
    echo "Please set \$KAFKA_HOME environment variable properly"
    exit
fi

TOPIC=network-data
PARTITIONS=4
REPLICATION=1

$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic $TOPIC --partitions $PARTITIONS --replication-factor $REPLICATION
