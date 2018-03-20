#!/bin/bash
KAFKA_VERSION=kafka_2.11-1.0.0
wget http://mirror.vorboss.net/apache/kafka/1.0.0/$KAFKA_VERSION.tgz
tar xvf $KAFKA_VERSION.tgz
INSTALL_DIR=$(pwd)
export KAFKA_HOME=$INSTALL_DIR/$KAFKA_VERSION
echo "export KAFKA_HOME=$KAFKA_HOME"
