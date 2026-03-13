#!/bin/bash

CONNECTORS_DIR="./connectors"
mkdir -p $CONNECTORS_DIR

echo "Downloading Flink Kafka connector..."
wget -P $CONNECTORS_DIR https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/3.0.2-1.18/flink-sql-connector-kafka-3.0.2-1.18.jar

echo "Downloading Flink JDBC connector..."
wget -P $CONNECTORS_DIR https://repo.maven.apache.org/maven2/org/apache/flink/flink-connector-jdbc/3.1.2-1.18/flink-connector-jdbc-3.1.2-1.18.jar

echo "Downloading PostgreSQL JDBC driver..."
wget -P $CONNECTORS_DIR https://jdbc.postgresql.org/download/postgresql-42.7.1.jar

echo "All connectors downloaded successfully!"