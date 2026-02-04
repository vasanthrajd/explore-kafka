#!/bin/bash

docker exec kafka-1 kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic transactions \
  --partitions 3 \
  --replication-factor 1 \
  --config cleanup.policy=delete \
  --config retention.ms=86400000

docker exec kafka-1 kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic account-balances \
  --partitions 3 \
  --replication-factor 1 \
  --config cleanup.policy=compact \
  --config min.compaction.lag.ms=60000

echo "Topics created successfully"