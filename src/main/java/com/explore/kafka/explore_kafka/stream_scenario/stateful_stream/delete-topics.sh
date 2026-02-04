#!/bin/bash

docker exec -it kafka-1 kafka-topics --bootstrap-server localhost:9092 --delete --topic transactions

docker exec -it kafka-1 kafka-topics --bootstrap-server localhost:9092 --delete --topic account-balances

docker exec -it kafka-1 kafka-topics --bootstrap-server localhost:9092 --delete --topic __transaction_state

docker exec -it kafka-1 kafka-topics --bootstrap-server localhost:9092 --delete --topic stateful-transaction-processor-account-balance-store-changelog
echo "Topics deleted successfully"