#!/bin/bash

echo "Submitting Flink SQL job..."

docker-compose exec -T jobmanager bin/sql-client.sh -f //opt/flink/sql-jobs/fraud-detection.sql

echo "Job submitted! Check Flink UI at http://localhost:8081"