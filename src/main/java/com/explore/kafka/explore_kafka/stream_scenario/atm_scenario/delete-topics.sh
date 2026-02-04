#!/bin/bash

docker exec -it kafka-1 kafka-topics --bootstrap-server localhost:9092 --delete --topic withdrawal-requests

docker exec -it kafka-1 kafka-topics --bootstrap-server localhost:9092 --delete --topic withdrawal-responses

echo "Topics deleted successfully"