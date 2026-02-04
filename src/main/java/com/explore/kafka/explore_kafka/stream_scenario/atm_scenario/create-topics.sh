#!/bin/bash

docker exec kafka-1 kafka-topics --create --bootstrap-server localhost:9092 --topic withdrawal-requests --partitions 3 --replication-factor 1

docker exec kafka-1 kafka-topics --create --bootstrap-server localhost:9092 --topic withdrawal-responses --partitions 3 --replication-factor 1