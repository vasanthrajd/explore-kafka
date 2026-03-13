#!/bin/bash

echo "Waiting for Kafka to be ready..."
sleep 10

echo "Creating Kafka topics..."

docker exec kafka kafka-topics --create --topic orders --partitions 6 --replication-factor 1 --bootstrap-server localhost:29092

docker exec kafka kafka-topics --create --topic payment_events --partitions 6 --replication-factor 1 --bootstrap-server localhost:29092

docker exec kafka kafka-topics --create --topic fraud_alerts --partitions 3 --replication-factor 1 --bootstrap-server localhost:29092

docker exec kafka kafka-topics --create --topic enriched_orders --partitions 6 --replication-factor 1 --bootstrap-server localhost:29092

docker exec kafka kafka-topics --create --topic order_metrics --partitions 3 --replication-factor 1 --bootstrap-server localhost:29092

echo "Topics created successfully!"

# List all topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:29092