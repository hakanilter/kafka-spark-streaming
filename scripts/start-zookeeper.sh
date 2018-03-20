#!/bin/bash

if [ -z "$KAFKA_HOME" ]
  then
    echo "Please set \$KAFKA_HOME environment variable properly"
    exit
fi

$KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties
